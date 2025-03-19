package usecase

import (
	"go.uber.org/fx"

	cacheRepo "example.com/seminar07/app02/internal/api/repository/cache"
	slowStorageRepo "example.com/seminar07/app02/internal/api/repository/slowstorage"
	"example.com/seminar07/app02/internal/config"
)

func ProvideCache(cache *cacheRepo.Cache) Cache {
	return cache
}

func ProvideSlowStorage(slowStorage slowStorageRepo.EmulatedRepository) SlowStorage {
	return slowStorage
}

func ProvideConfig(cfg *config.Config) config.Listen {
	return cfg.Listen
}

func FxModule() fx.Option {
	return fx.Module(
		"api",
		cacheRepo.FxModule(),
		slowStorageRepo.FxModule(),
		fx.Provide(
			fx.Private,
			ProvideCache,
			ProvideSlowStorage,
			ProvideConfig,
			NewHandler,
		),
		fx.Provide(NewRouter),
		fx.Invoke(Start),
	)
}
