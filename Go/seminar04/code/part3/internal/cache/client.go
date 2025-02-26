package cache

import (
	"fmt"

	"github.com/redis/go-redis/v9"

	"example.com/seminar04/part3/internal/config"
)

func NewClient(cfg config.Cache) *redis.Client {
	return redis.NewClient(&redis.Options{
		Addr:     fmt.Sprintf("%s:%d", cfg.Host, cfg.Port),
		Username: cfg.User,
		Password: cfg.Password,
	})
}
