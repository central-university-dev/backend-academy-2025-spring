package config

type Config struct {
	Cache   Cache   `envPrefix:"CACHE_"`
	Listen  Listen  `envPrefix:"LISTEN_"`
	Tracing Tracing `envPrefix:"TRACING_"`
}
