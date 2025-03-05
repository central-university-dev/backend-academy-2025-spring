package config

import (
	"bytes"
	_ "embed"
	"errors"
	"fmt"

	"github.com/caarlos0/env/v11"
	"github.com/spf13/viper"
)

type (
	Config struct {
		Database Database `yaml:"database"`
	}

	Database struct {
		Host     string `yaml:"host" env:"DB_HOST" envDefault:"localhost"`
		Port     int    `yaml:"port" env:"DB_PORT" envDefault:"5432"`
		Username string `yaml:"username" env:"DB_USERNAME" envDefault:"admin"`
		Password string `yaml:"password" env:"DB_PASSWORD" envDefault:"admin"`
		Name     string `yaml:"name" env:"DB_NAME" envDefault:"seminar05"`
	}
)

func (d *Database) ToDSN() string {
	return fmt.Sprintf("postgresql://%s:%s@%s:%d/%s?target_session_attrs=read-write&sslmode=disable",
		d.Username,
		d.Password,
		d.Host,
		d.Port,
		d.Name,
	)
}

func NewConfigFromEnv() (*Config, error) {
	cfg := &Config{}

	if err := env.Parse(cfg); err != nil {
		return nil, fmt.Errorf("parse config: %w", err)
	}

	return cfg, nil
}

var (
	//go:embed default-config.yaml
	configBytes []byte
)

func NewConfigFromFile(name string) (*Config, error) {
	cfg := &Config{}

	v := viper.New()

	v.SetConfigType("yaml")

	v.SetConfigFile(name)

	if err := v.ReadConfig(bytes.NewBuffer(configBytes)); err != nil {
		return nil, fmt.Errorf("read config: %w", err)
	}

	if err := v.MergeInConfig(); err != nil {
		if errors.Is(err, &viper.ConfigParseError{}) {
			return nil, fmt.Errorf("merge config: %w", err)
		}
	}

	if err := v.Unmarshal(cfg); err != nil {
		return nil, fmt.Errorf("unmarshal config: %w", err)
	}

	return cfg, nil
}
