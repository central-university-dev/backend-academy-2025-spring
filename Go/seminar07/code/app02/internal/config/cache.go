package config

type Cache struct {
	Capacity int `env:"CAPACITY,required"`
}
