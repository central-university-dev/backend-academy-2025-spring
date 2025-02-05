package main

import (
	"go.uber.org/fx"
	"go.uber.org/fx/fxevent"
	"go.uber.org/zap"
)

func main() {
	appOpts := fx.Options(
		fx.Provide(NewConfig),
		fx.Provide(NewLogger),
		fx.Provide(NewEchoHandler),
		fx.Provide(NewMux),
		fx.Provide(NewServer),
		fx.Invoke(RunServer),
		fx.WithLogger(func(log *zap.Logger) fxevent.Logger {
			return &fxevent.ZapLogger{Logger: log}
		}),
	)

	fx.New(appOpts).Run()
}
