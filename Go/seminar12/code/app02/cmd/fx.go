package main

import (
	"os"

	"go.uber.org/fx"
)

func BuildApp() fx.Option {
	return fx.Options(
		fx.Provide(FromEnv),
		fx.Provide(NewListToHandle),
		fx.Invoke(TraceConfigAndRuntimeData),
		fx.Invoke(HandleListConcurrently),
		fx.Invoke(func() {
			os.Exit(0)
		}),
	)
}
