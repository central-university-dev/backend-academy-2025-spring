package main

import (
	"context"
	"errors"
	"fmt"
	"io"
	"log/slog"
	"os/signal"
	"syscall"

	"github.com/segmentio/kafka-go"
)

func main() {
	var closers []func() error

	// Конфигурация ридера достаточно обширная и включает много таймаутов, логгеры, настройки размера сообщений и т.д.
	// Для большинства параметров задаются значения по умолчанию, если не задать явно.
	// Я привожу здесь основные, которые вам могут понадобиться в проекте.
	reader := kafka.NewReader(kafka.ReaderConfig{
		Brokers:  []string{"localhost:9092", "localhost:9093"},
		Topic:    "seminar.external.events",
		MinBytes: 0,    // 10KB
		MaxBytes: 10e6, // 10MB
		// С какого оффсета начинать чтение для определённой GroupID.
		// Такая настройка автоматически вычитает все незакоммиченные сообщения из брокера при старте консьюмера.
		StartOffset: kafka.LastOffset,
		// Консьюмер группа. Если не задать, reader не сможет установить оффсет и будет при запуске вычитывать
		// все сообщения из топика.
		GroupID: "seminar.external.events.1",
	})

	closers = append(closers, reader.Close)

	ctx, cancel := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer cancel()

	go baseConsumer{reader}.start(ctx)

	<-ctx.Done()

	for _, closer := range closers {
		if err := closer(); err != nil {
			slog.Error("Error closing consumer", err)
		}
	}
}

// Для простоты я взял только метод чтения новых сообщений из брокера из всего kafka.Reader.
// Создал его просто для того, чтобы показать как можно создать самостоятельно аналог интерцепторов из sarama
// (цепочка ответственности).
type consumer interface {
	ReadMessage(context.Context) (kafka.Message, error)
	CommitMessages(ctx context.Context, msgs ...kafka.Message) error
}

type baseConsumer struct {
	consumer
}

func (c baseConsumer) start(ctx context.Context) {
	for {
		select {
		case <-ctx.Done():
			return
		default:
		}

		msg, err := c.ReadMessage(ctx)
		if err != nil {
			if errors.Is(err, io.EOF) {
				return
			}

			slog.Error("Error reading message", err)
			continue
		}

		// Если не вызвать коммит на прочитанные сообщения, то они оффсет для консьюмер группы будет
		// всегда оставаться 0. То есть будут вычитываться все сообщения при старте.
		if err = c.CommitMessages(ctx, msg); err != nil {
			slog.Error("Error commit message", err)
		}

		fmt.Println(string(msg.Value))
	}
}
