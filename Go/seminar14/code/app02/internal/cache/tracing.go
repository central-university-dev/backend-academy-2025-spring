package cache

import (
	"github.com/redis/go-redis/extra/redisotel/v9"
	"github.com/redis/go-redis/v9"
)

func InstrumentTracing(client *redis.Client) error {
	return redisotel.InstrumentTracing(client)
}
