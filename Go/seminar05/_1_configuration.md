# Управление конфигурацией проекта

## ENV - предпочтительный вариант

Когда надо использовать секреты (токены, пароли и т.д.). env экспортируются в окружение на этапе CI.
Если приложение запускается к k8s, то это будет наиболее частый вариант определения параметров конфигурации.

Пример чтения конфигурации из переменных окружения

```go
package config

import (
	"fmt"

	"github.com/caarlos0/env/v11"
)

type (
	Config struct {
		Database Database `yaml:"database"`
	}

	Database struct {
		Host     string `env:"DB_HOST" envDefault:"localhost"`
		Port     int    `env:"DB_PORT" envDefault:"5432"`
		Username string `env:"DB_USERNAME" envDefault:"admin"`
		Password string `env:"DB_PASSWORD" envDefault:"admin"`
		Name     string `env:"DB_NAME" envDefault:"seminar05"`
	}
)

func NewConfigFromEnv() (*Config, error) {
	cfg := &Config{}

	if err := env.Parse(cfg); err != nil {
		return nil, fmt.Errorf("parse config: %w", err)
	}

	return cfg, nil
}
```

## Чтение конфигурации из файлов

Часто используется, если приложение запускается на выделенном сервере. Это удобно и просто.

библиотека `spf13/viper` фактически стандарт в отрасли.

Пример в `app/file/main.go` и `config/config.go`

## Команды

```bash
cd Go/seminar05
go build -o env_example ./app/env/*.go
go build -o file_example ./app/file/*go

./env_example
DB_HOST=192.168.0.14 DB_PORT=12345 ./env_example

./file_example
./file_example --config ./app/file/config.yaml
```