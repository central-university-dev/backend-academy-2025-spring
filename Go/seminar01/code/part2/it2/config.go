package main

type Config struct {
	Address string
}

func NewConfig() Config {
	return Config{Address: ":8080"}
}
