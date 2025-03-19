package config

import (
	"time"
)

type SlowStorage struct {
	Delay time.Duration `env:"DELAY" envDefault:"1s"`
}
