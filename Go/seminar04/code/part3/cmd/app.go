package main

import (
	"go.uber.org/fx"

	"example.com/seminar04/part3/internal/api/usecase"
	"example.com/seminar04/part3/internal/cache"
	"example.com/seminar04/part3/internal/config"
	"example.com/seminar04/part3/internal/logger"
)

func BuildApp() fx.Option {
	return fx.Options(
		config.FxModule(),
		logger.FxModule(),
		cache.FxModule(),
		usecase.FxModule(),
	)
}
