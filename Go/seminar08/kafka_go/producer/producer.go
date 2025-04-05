package main

import (
	"context"
	"fmt"
	"hash/adler32"
	"log/slog"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/segmentio/kafka-go"
)

func main() {
	var closers []func() error

	// Настроек продюсера много. Можно посмотреть самостоятельно на таймауты, логгеры и т.д. Я покажу основные
	// параметры, используемые почти всегда.
	writer := &kafka.Writer{
		// Адреса брокеров.
		Addr: kafka.TCP("localhost:9092", "localhost:9093"),
		// Топик. Надо определить его либо на весь Writer, либо в каждом сообщении.
		// Мы у себя в проекте определяем в сообщениях, чтобы унифицировать отправку сообщений во все топики.
		Topic: "seminar.external.events",
		// Балансировщик для выбора номера партиции. Можно посмотреть его интерфейс - он простой, всего 1 метод.
		// Кроме того, библиотека определяет BalancerFunc. Её можно имплементировать у себя и она уже реализует
		// нужный интерфейс.
		// Также есть балансировщики, уже определённые в библиотеке (хеш, раундробин и т.д.)
		// Либо можно задавать партицию для записи в самом сообщении, если не определять балансировщик здесь.
		// Ниже показал пример стандартного балансировщика на основе хэша сообщения.
		Balancer: &kafka.Hash{Hasher: adler32.New()},
		// Можно самому конфигурировать транспорт (kafka.Transport{}). Там задаются параметры таймаутов,
		// шифрования и метаданных топика.
		Transport: kafka.DefaultTransport,
	}

	ctx, cancel := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer cancel()

	logger := slog.New(slog.NewTextHandler(os.Stderr, &slog.HandlerOptions{}))
	// Строим цепочку ответственности, последним вызовом в которой будет вызов writer.
	go baseProducer{
		produceWithLog{writer, logger},
	}.start(ctx)

	<-ctx.Done()

	for _, closer := range closers {
		if err := closer(); err != nil {
			slog.Error("Error closing consumer", err)
		}
	}
}

// В отличие от sarama, продюсер kafka-go реализует по сути только 1 значимый метод - запись сообщений в брокер.
// Поэтому мы создаём свой интерфейс, чтобы можно было реализовать паттерн цепочка ответственности.
type producer interface {
	WriteMessages(context.Context, ...kafka.Message) error
}

type baseProducer struct {
	producer
}

func (p baseProducer) start(ctx context.Context) {
	for {
		var toSend string
		_, _ = fmt.Fscanln(os.Stdin, &toSend)

		// Поля очень похожи на реализацию сообщения в sarama.
		// Описывать их дополнительно здесь не буду - можно посмотреть там.
		msg := kafka.Message{
			//Topic: "seminar.external.events",
			Key:   []byte("key-go-kafka"),
			Value: []byte(toSend),
			Time:  time.Now(),
			Headers: []kafka.Header{
				{
					Key:   "test-header",
					Value: []byte("test-value"),
				},
			},
			Partition: lengthPartitioner([]byte(toSend)),
		}

		_ = p.WriteMessages(ctx, msg)
	}
}

// Реализация интерфейса producer - логирует отправку сообщения.
type produceWithLog struct {
	producer
	logger *slog.Logger
}

func (p produceWithLog) WriteMessages(ctx context.Context, msgs ...kafka.Message) error {
	err := p.producer.WriteMessages(ctx, msgs...)
	if err != nil {
		p.logger.Error("Error producing message", err)
	} else {
		p.logger.Info("Produced message", "len", len(msgs))
	}

	return err
}

// Простой пример выбора партиции на основе сообщения.
func lengthPartitioner(msg []byte) int {
	const partitionsNum = 3

	return len(msg) % partitionsNum
}
