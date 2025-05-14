package repository

import (
	"github.com/redis/go-redis/v9"
	"go.uber.org/fx"
)

func FxModule() fx.Option {
	return fx.Module(
		"api-repo",
		fx.Provide(
			fx.Private,
			ProvideCacheClient,
		),
		fx.Provide(NewRepository),
	)
}

func ProvideCacheClient(realCacheClient *redis.Client) CacheClient {
	return realCacheClient
}
