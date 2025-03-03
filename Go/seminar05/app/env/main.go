package main

import (
	"errors"
	"fmt"
	"log"
	"os"

	"example.com/seminar05/config"
)

func main() {
	if err := setEnv(); err != nil {
		log.Fatal(err)
	}

	cfg, err := config.NewConfigFromEnv()
	if err != nil {
		log.Fatal(err)
	}

	fmt.Println(cfg.Database.ToDSN())
}

func setEnv() error {
	return errors.Join(
		os.Setenv("DB_HOST", getEnv("DB_HOST", "127.0.0.1")),
		os.Setenv("DB_PORT", getEnv("DB_PORT", "5432")),
		os.Setenv("DB_USERNAME", getEnv("DB_USERNAME", "postgres")),
		os.Setenv("DB_PASSWORD", getEnv("DB_PASSWORD", "postgres")),
		os.Setenv("DB_NAME", getEnv("DB_NAME", "postgres")),
	)
}

func getEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return fallback
}
