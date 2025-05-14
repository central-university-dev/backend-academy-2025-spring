package usecase

import (
	"go.uber.org/fx"

	"example.com/seminar14/app01/internal/config"
)

func ProvideConfig(cfg *config.Config) config.Listen {
	return cfg.Listen
}

func FxModule() fx.Option {
	return fx.Module(
		"api",
		fx.Provide(
			fx.Private,
			ProvideConfig,
			NewHandler,
		),
		fx.Provide(NewRouter),
		fx.Invoke(Start),
	)
}
