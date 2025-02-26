package cache

import (
	"context"
	"fmt"

	"github.com/redis/go-redis/v9"
)

func Ping(client *redis.Client) error {
	if err := client.Ping(context.Background()).Err(); err != nil {
		return fmt.Errorf("failed to check if cache is available: %w", err)
	}

	return nil
}
