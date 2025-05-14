package main

import (
	"go.uber.org/fx"

	"example.com/seminar14/app02/internal/api/usecase"
	"example.com/seminar14/app02/internal/cache"
	"example.com/seminar14/app02/internal/config"
	"example.com/seminar14/app02/internal/tracing"
)

func BuildApp() fx.Option {
	return fx.Options(
		config.FxModule(),
		tracing.FxModule(),
		cache.FxModule(),
		usecase.FxModule(),
	)
}
