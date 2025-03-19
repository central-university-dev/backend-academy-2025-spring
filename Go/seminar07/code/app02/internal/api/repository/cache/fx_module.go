package cache

import (
	"go.uber.org/fx"

	"example.com/seminar07/app02/internal/config"
)

func ProvideConfig(cfg *config.Config) config.Cache {
	return cfg.Cache
}

func FxModule() fx.Option {
	return fx.Module(
		"cache",
		fx.Provide(
			fx.Private,
			ProvideConfig,
		),
		fx.Provide(New),
	)
}
