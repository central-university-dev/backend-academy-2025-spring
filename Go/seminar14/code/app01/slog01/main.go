package main

import (
	"io"
	"log/slog"
	"os"
)

func DefaultWriter() io.Writer {
	return os.Stderr
}

func DefaultJSONHandler(writer io.Writer) *slog.JSONHandler {
	return slog.NewJSONHandler(
		writer,
		&slog.HandlerOptions{
			Level: slog.LevelDebug,
		},
	)
}

func NewLogger() *slog.Logger {
	return slog.New(DefaultJSONHandler(DefaultWriter()))
}

func main() {
	log := NewLogger()

	log.Info(
		"order received",
		slog.String("customer", "Vasya Pupkin"),
		slog.Int("order_id", 123),
	)
	log.Info(
		"order received",
		"customer", "Vasya Pupkin",
		"order_id", 123,
	)
}
