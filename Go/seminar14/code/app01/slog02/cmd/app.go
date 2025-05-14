package main

import (
	"go.uber.org/fx"

	"example.com/seminar14/app01/internal/api/usecase"
	"example.com/seminar14/app01/internal/config"
	"example.com/seminar14/app01/internal/logger"
)

func BuildApp() fx.Option {
	return fx.Options(
		config.FxModule(),
		logger.FxModule(),
		usecase.FxModule(),
	)
}
