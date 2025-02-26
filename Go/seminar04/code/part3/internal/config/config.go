package config

type Config struct {
	Cache  Cache  `envPrefix:"CACHE_"`
	Listen Listen `envPrefix:"LISTEN_"`
}
