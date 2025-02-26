package config

import (
	"fmt"

	"github.com/caarlos0/env/v9"
)

func FromEnv() (*Config, error) {
	var result Config
	if err := env.Parse(&result); err != nil {
		return nil, fmt.Errorf("failed to parse env data: %w", err)
	}

	return &result, nil
}
