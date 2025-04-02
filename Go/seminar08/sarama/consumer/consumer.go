package main

import (
	"context"
	"fmt"
	"log/slog"
	"os/signal"
	"syscall"

	"github.com/IBM/sarama"
)

func main() {
	// Конфиг в библиотеке общий для продюсера и консьюмера, но здесь задаются параметры второго.
	config := sarama.NewConfig()
	// Активен ли канал с ошибками обработки.
	config.Consumer.Return.Errors = true
	// Промежуточные функции, которые будут выполняться в момент получения сообщения до получения его в канале.
	// Логирование, обработка хедеров и т.д.
	config.Consumer.Interceptors = []sarama.ConsumerInterceptor{
		&loggerInterceptor{slog.With("consumer", "interceptor")},
		&headersPrinter{slog.With("headers", "printer")},
	}

	var closers []func() error

	// Фактически промежуточный интерфейс, который позволяет получить все нужные данные о возможностях консьюмера
	// в кластере: какие есть топики, сколько в каждом партиций и т.д.
	// Позволяет управлять этими топиками и подписываться на получение сообщений из них.
	consumer, err := sarama.NewConsumer([]string{"localhost:9092", "localhost:9093"}, config)
	if err != nil {
		panic("Failed to start consumer: " + err.Error())
	}
	closers = append(closers, consumer.Close)

	ctx, cancel := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer cancel()

	// Здесь мы запускаем чтение на каждую из партиций в каждом топике. Просто для просмотра возможностей библиотеки.
	// В реальной жизни чтение из каждого топика вы будете запускать отдельно, скорее всего. Потому что
	// сервисы редко используют все топики кластера.
	// Я закомментирую получение топиков, потому что здесь мы пробуем читать ВСЕ топики, в том числе и технические.
	// topics, err := consumer.Topics()
	// if err != nil {
	//   panic(err)
	// }

	topics := []string{"seminar.internal.events"}

	for _, topic := range topics {
		// Эта библиотека вынуждает нас читать из каждой партиции в отдельности.
		// Но, благодаря этому, мы отчётливо понимаем гарантии порядка в доставке сообщений.
		topikPartitions, err := consumer.Partitions(topic)
		if err != nil {
			panic(err)
		}

		for _, partitionNum := range topikPartitions {
			go consume(ctx, consumer, topic, partitionNum)
		}
	}

	<-ctx.Done()

	for _, closer := range closers {
		if err := closer(); err != nil {
			slog.Error("Error closing consumer", err)
		}
	}
}

func consume(ctx context.Context, consumer sarama.Consumer, topic string, partition int32) {
	// ConsumePartition подписываемся на получение сообщений с конкретной партиции топика.
	partitionConsumer, err := consumer.ConsumePartition(topic, partition, sarama.OffsetNewest)
	if err != nil {
		panic("Failed to start partition consumer: " + err.Error())
	}

	slog.Info("start consume on", "topic", topic, "partition", partition)

	for {
		select {
		case msg := <-partitionConsumer.Messages():
			fmt.Println(string(msg.Value))
		case err := <-partitionConsumer.Errors():
			slog.Error("Error from consumer", err.Err)
		case <-ctx.Done():
			_ = partitionConsumer.Close()
			return
		}
	}
}

// Интерцептор логирует параметры сообщения.
type loggerInterceptor struct {
	*slog.Logger
}

func (l *loggerInterceptor) OnConsume(m *sarama.ConsumerMessage) {
	l.Info("message", "partition", m.Partition, "key", m.Key, "len", len(m.Value))
}

// Интерцептор логирует все хедеры.
type headersPrinter struct {
	*slog.Logger
}

func (l *headersPrinter) OnConsume(m *sarama.ConsumerMessage) {
	for _, h := range m.Headers {
		l.Info("headers", "key", string(h.Key), "value", string(h.Value))
	}
}
