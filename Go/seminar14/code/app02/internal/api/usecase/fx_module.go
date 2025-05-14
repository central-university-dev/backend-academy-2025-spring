package usecase

import (
	"go.uber.org/fx"

	"example.com/seminar14/app02/internal/api/repository"
)

func FxModule() fx.Option {
	return fx.Module(
		"api-usecase",
		repository.FxModule(),
		fx.Provide(
			fx.Private,
			ProvideRepository,
			NewHandler,
		),
		fx.Provide(NewRouter),
		fx.Invoke(Start),
	)
}

func ProvideRepository(realRepository repository.Repository) Repository {
	return realRepository
}
