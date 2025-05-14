package cache

import (
	"go.uber.org/fx"

	"example.com/seminar14/app02/internal/config"
)

func FxModule() fx.Option {
	return fx.Module(
		"cache",
		fx.Provide(
			fx.Private,
			ProvideConfig,
		),
		fx.Provide(NewClient),
		fx.Invoke(InstrumentTracing),
		fx.Invoke(Ping),
	)
}

func ProvideConfig(cfg *config.Config) config.Cache {
	return cfg.Cache
}
