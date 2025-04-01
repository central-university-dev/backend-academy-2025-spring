package slowStorage

import (
	"go.uber.org/fx"

	"example.com/seminar07/app02/internal/config"
)

func ProvideConfig(cfg *config.Config) config.SlowStorage {
	return cfg.SlowStorage
}

func FxModule() fx.Option {
	return fx.Module(
		"slow-storage",
		fx.Provide(
			fx.Private,
			ProvideConfig,
		),
		fx.Provide(NewRepository),
	)
}
