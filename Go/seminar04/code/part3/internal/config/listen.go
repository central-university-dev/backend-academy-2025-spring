package config

type Listen struct {
	Network string `env:"NETWORK" envDefault:"tcp"`
	Port    int    `env:"PORT" envDefault:"8080"`
}
