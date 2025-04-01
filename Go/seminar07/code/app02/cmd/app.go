package main

import (
	"go.uber.org/fx"

	"example.com/seminar07/app02/internal/api/usecase"
	"example.com/seminar07/app02/internal/config"
	"example.com/seminar07/app02/internal/logger"
)

func BuildApp() fx.Option {
	return fx.Options(
		config.FxModule(),
		logger.FxModule(),
		usecase.FxModule(),
	)
}
