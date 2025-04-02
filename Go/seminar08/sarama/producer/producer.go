package main

import (
	"context"
	"fmt"
	"log"
	"log/slog"
	"math/rand/v2"
	"os"
	"os/signal"
	"syscall"

	"github.com/IBM/sarama"
	"github.com/google/uuid"
)

func main() {
	// Инициализируем конфигурацию. Состоит из нескольких разделов. Здесь мы используем настройки только Producer.
	// Для всех значений параметра читайте комментарии в коде библиотеки. Они вполне исчерпывающе описывают поведение.
	config := sarama.NewConfig()
	// Тип подтверждения записи в кафку. Читайте комментарии в коде
	config.Producer.RequiredAcks = sarama.WaitForAll
	// Количество попыток отправки сообщения.
	config.Producer.Retry.Max = 10
	// Return устанавливает, какие каналы ответа от кафки будут активны.
	config.Producer.Return.Successes = true

	// Partitioner - святая святых) выбор партиции для записи.
	// По умолчанию используется sarama.NewHashPartitioner. Вы можете подобрать любой подходящий для вашей задачи.
	// Я ниже привёл 2 варианта:
	// - самостоятельно написанная функция выбора партиции
	config.Producer.Partitioner = func(topic string) sarama.Partitioner {
		return &randomPartitioner{rand.New(rand.NewPCG(0, 1))}
	}
	// - sarama.NewManualPartitioner - номер партиции будет браться из сообщения (по умолчанию там будет 0)
	config.Producer.Partitioner = sarama.NewManualPartitioner
	// Интерцепторы выполняются до отправки сообщения. Так в них можно добавить любые действия - логирование,
	// добавить что-то в headers и т.д.
	config.Producer.Interceptors = []sarama.ProducerInterceptor{
		&produceWithLog{slog.New(slog.NewTextHandler(os.Stderr, &slog.HandlerOptions{}))},
		&produceWithHeader{},
	}

	var closers []func() error

	// SyncProducer блокирует отправку сообщений, пока не будет подтверждена отправка предыдущего.
	// Помогает сохранять порядок сообщений, но работает медленнее.
	// AsyncProducer - не блокирует отправку. Ошибки придут в канал Errors() - смотрите интерфейс продюсера.
	producer, err := sarama.NewSyncProducer([]string{"localhost:9092", "localhost:9093"}, config)
	if err != nil {
		log.Fatalf("Failed to start producer: %v", err)
	}
	closers = append(closers, producer.Close)

	ctx, cancel := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer cancel()

	go syncProducer{producer}.start(ctx)

	<-ctx.Done()

	for _, closer := range closers {
		if err := closer(); err != nil {
			slog.Error("Error closing consumer", err)
		}
	}
}

type syncProducer struct {
	sarama.SyncProducer
}

func (p syncProducer) start(ctx context.Context) {
	for {
		select {
		case <-ctx.Done():
			return
		default:
			var toSend string
			_, _ = fmt.Fscanln(os.Stdin, &toSend)

			msg := &sarama.ProducerMessage{
				Topic: "seminar.internal.events",
				// По ключу вы на приёмной стороне будете понимать в какую структуру десериализовать сообщение.
				Key:   sarama.StringEncoder("test-key"),
				Value: sarama.StringEncoder(toSend),
				// В Headers хранится вся информация, которая не относится к основному сообщению.
				Headers: []sarama.RecordHeader{
					{
						Key:   []byte("test-header"),
						Value: []byte("test-value"),
					},
				},
				// Если в конфиге указать NewManualPartitioner, номер партиции будет задаваться здесь.
				Partition: lengthPartitioner([]byte(toSend)),
			}

			_, _, _ = p.SendMessage(msg)
		}
	}
}

// Простой пример выбора партиции на основе сообщения.
func lengthPartitioner(msg []byte) int32 {
	const partitionsNum = 3

	return int32(len(msg) % partitionsNum)
}

type produceWithLog struct {
	logger *slog.Logger
}

func (p produceWithLog) OnSend(msg *sarama.ProducerMessage) {
	p.logger.Info("Produced message", "topic", msg.Topic, "partition", msg.Partition, "offset", msg.Offset)
}

type produceWithHeader struct{}

func (p produceWithHeader) OnSend(msg *sarama.ProducerMessage) {
	msg.Headers = append(msg.Headers,
		sarama.RecordHeader{
			Key:   []byte("test-header"),
			Value: []byte(uuid.New().String()),
		})
}

// Наивная реализация собственного Partitioner.
type randomPartitioner struct {
	*rand.Rand
}

func (r *randomPartitioner) Partition(message *sarama.ProducerMessage, numPartitions int32) (int32, error) {
	return r.Int32N(numPartitions), nil
}

func (r *randomPartitioner) RequiresConsistency() bool {
	return true
}
