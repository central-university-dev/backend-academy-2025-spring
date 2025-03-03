package main

import (
	"flag"
	"fmt"
	"log"

	"example.com/seminar05/config"
)

func main() {
	configFileName := flag.String("config", "", "path to config file")

	flag.Parse()

	cfg, err := config.NewConfigFromFile(*configFileName)
	if err != nil {
		log.Fatal(err)
	}

	fmt.Println(cfg.Database.ToDSN())
}
