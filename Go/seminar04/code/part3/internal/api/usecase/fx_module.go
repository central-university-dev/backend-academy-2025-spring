package usecase

import (
	"go.uber.org/fx"

	"example.com/seminar04/part3/internal/api/repository"
	"example.com/seminar04/part3/internal/config"
)

func FxModule() fx.Option {
	return fx.Module(
		"api-usecase",
		repository.FxModule(),
		fx.Provide(
			fx.Private,
			ProvideConfig,
			ProvideRepository,
			NewGetHandler,
			NewSetHandler,
			NewRouter,
		),
		fx.Provide(NewServer),
		fx.Invoke(TouchServer),
	)
}

func ProvideConfig(cfg *config.Config) config.Listen {
	return cfg.Listen
}

func ProvideRepository(realRepository repository.Repository) Repository {
	return realRepository
}
