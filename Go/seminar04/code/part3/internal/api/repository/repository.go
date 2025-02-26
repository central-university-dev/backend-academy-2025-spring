package repository

import (
	"context"
	"time"

	"github.com/redis/go-redis/v9"
)

type CacheClient interface {
	Get(ctx context.Context, key string) *redis.StringCmd
	Set(ctx context.Context, key string, value any, expiration time.Duration) *redis.StatusCmd
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

func (r Repository) Set(ctx context.Context, key string, value string) error {
	return r.cacheClient.Set(ctx, key, value, time.Duration(0)).Err()
}
