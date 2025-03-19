package config

type Listen struct {
	Port int `env:"PORT" envDefault:"8080"`
}
