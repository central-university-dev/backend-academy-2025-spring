package repository

import (
	"context"
	"fmt"
	"time"

	"github.com/redis/go-redis/v9"
)

//go:generate mockery --name RedisClient --structname MockRedisClient --filename mock_redis_client_test.go --outpkg repository_test --output .
type RedisClient interface {
	Get(ctx context.Context, key string) *redis.StringCmd
	Set(ctx context.Context, key string, value any, expiration time.Duration) *redis.StatusCmd
	Ping(ctx context.Context) *redis.StatusCmd
}

type Repository struct {
	redisClient RedisClient
}

func NewRepository(redisClient RedisClient) Repository {
	return Repository{redisClient: redisClient}
}

func (r Repository) IsAvailable(ctx context.Context) (bool, error) {
	if err := r.redisClient.Ping(ctx).Err(); err != nil {
		return false, fmt.Errorf("failed to ping redis: %w", err)
	}

	return true, nil
}

func (r Repository) GetValue(ctx context.Context, key string) (string, error) {
	value, err := r.redisClient.Get(ctx, key).Result()
	if err != nil {
		return "", fmt.Errorf("failed to get value for key %s: %w", key, err)
	}

	return value, nil
}

func (r Repository) SetValue(ctx context.Context, key string, value string) error {
	if err := r.redisClient.Set(ctx, key, value, 0).Err(); err != nil {
		return fmt.Errorf("failed to set value for key %s: %w", key, err)
	}

	return nil
}
