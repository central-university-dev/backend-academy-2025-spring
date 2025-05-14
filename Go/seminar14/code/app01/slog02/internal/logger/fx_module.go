package logger

import (
	"go.uber.org/fx"
)

func FxModule() fx.Option {
	return fx.Module(
		"logger",
		fx.Provide(NewLogger),
		fx.Invoke(SetAsDefault),
	)
}
