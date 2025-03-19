package config

type Config struct {
	Listen      Listen      `envPrefix:"LISTEN_"`
	SlowStorage SlowStorage `envPrefix:"SLOW_STORAGE_"`
	Cache       Cache       `envPrefix:"CACHE_"`
}
