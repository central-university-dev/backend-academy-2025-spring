package config

type Cache struct {
	Host     string `env:"HOST,required"`
	Port     int    `env:"PORT" envDefault:"6379"`
	User     string `env:"USER,required"`
	Password string `env:"PASSWORD,required"`
}
