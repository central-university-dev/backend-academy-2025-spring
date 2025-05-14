package repository

import (
	"context"

	"github.com/redis/go-redis/v9"
)

type CacheClient interface {
	Get(ctx context.Context, key string) *redis.StringCmd
}

type Repository struct {
	cacheClient CacheClient
}

func NewRepository(cacheClient CacheClient) Repository {
	return Repository{cacheClient: cacheClient}
}

func (r Repository) Get(ctx context.Context, key string) (string, error) {
	return r.cacheClient.Get(ctx, key).Result()
}
