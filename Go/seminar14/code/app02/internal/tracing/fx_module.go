package tracing

import (
	"go.uber.org/fx"

	"example.com/seminar14/app02/internal/config"
)

func FxModule() fx.Option {
	return fx.Module(
		"tracing",
		fx.Provide(
			fx.Private,
			ProvideConfig,
			NewClient,
			NewSpanExporter,
			NewResource,
		),
		fx.Provide(NewTraceProvider),
		fx.Invoke(SetDefaultTraceProvider),
		fx.Provide(NewTracer),
	)
}

func ProvideConfig(cfg *config.Config) config.Tracing {
	return cfg.Tracing
}
