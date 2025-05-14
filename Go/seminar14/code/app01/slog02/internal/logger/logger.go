package logger

import (
	"log/slog"
	"os"
)

func NewLogger() *slog.Logger {
	return slog.New(slog.NewJSONHandler(os.Stderr, nil))
}

func SetAsDefault(logger *slog.Logger) {
	slog.SetDefault(logger)
}
