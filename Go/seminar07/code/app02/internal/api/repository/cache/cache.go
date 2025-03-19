package cache

import (
	"fmt"

	lru "github.com/hashicorp/golang-lru/v2"

	"example.com/seminar07/app02/internal/config"
)

type Cache struct {
	*lru.Cache[string, int]
}

func New(cfg config.Cache) (*Cache, error) {
	inner, err := lru.New[string, int](cfg.Capacity)
	if err != nil {
		return nil, fmt.Errorf("failed to initialize cache: %w", err)
	}

	return &Cache{inner}, nil
}
