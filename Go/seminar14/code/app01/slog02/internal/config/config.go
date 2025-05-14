package config

type Config struct {
	Listen Listen `envPrefix:"LISTEN_"`
}
