package cache

import (
	"go.uber.org/fx"

	"example.com/seminar04/part3/internal/config"
)

func FxModule() fx.Option {
	return fx.Module(
		"cache",
		fx.Provide(
			fx.Private,
			ProvideConfig,
		),
		fx.Provide(NewClient),
		fx.Invoke(Ping),
	)
}

func ProvideConfig(cfg *config.Config) config.Cache {
	return cfg.Cache
}
