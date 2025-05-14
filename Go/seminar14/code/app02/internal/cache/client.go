package cache

import (
	"fmt"

	"github.com/redis/go-redis/v9"

	"example.com/seminar14/app02/internal/config"
)

func NewClient(cfg config.Cache) *redis.Client {
	return redis.NewClient(&redis.Options{
		Addr:     fmt.Sprintf("%s:%d", cfg.Host, cfg.Port),
		Username: cfg.User,
		Password: cfg.Password,
	})
}
