package main

import (
	"fmt"

	"github.com/caarlos0/env/v11"
)

type Config struct {
	ListSize       int `env:"LIST_SIZE,required"`
	WorkerPoolSize int `env:"WORKER_POOL_SIZE,required"`
	SlowFactor     int `env:"SLOW_FACTOR,required"`
}

func FromEnv() (*Config, error) {
	var result Config
	if err := env.Parse(&result); err != nil {
		return nil, fmt.Errorf("failed to parse env data: %w", err)
	}

	return &result, nil
}
