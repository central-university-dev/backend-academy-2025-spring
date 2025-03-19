package logger

import (
	"go.uber.org/zap"
)

func NewLogger() *zap.Logger {
	return zap.NewExample()
}
