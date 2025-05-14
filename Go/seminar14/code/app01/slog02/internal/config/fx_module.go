package config

import (
	"go.uber.org/fx"
)

func FxModule() fx.Option {
	return fx.Module(
		"config",
		fx.Provide(FromEnv),
	)
}
