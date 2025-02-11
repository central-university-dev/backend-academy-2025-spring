# go-swagger

https://github.com/go-swagger/go-swagger
http://goswagger.io/go-swagger/

## Общие сведения

`go-swagger` — это инструмент для работы с API, который позволяет автоматически генерировать документацию, 
клиентский и серверный код на основе спецификаций OpenAPI 2.0 (Swagger 2.0).

Особенности:

- Поддерживает только спецификацию в формате 2.0

## Установка

```bash
go install github.com/go-swagger/go-swagger/cmd/swagger@latest
```

## Использование

### Вызов command line утилиты
```bash
swagger generate server --spec=../../swag/api.yml --api-package api --model-package model --strict-responders --strict-additional-properties
```

### Используя go:generate
```go
package generate

//go:generate swagger generate server --spec=../../swag/api.yml --api-package api --model-package model --strict-responders --strict-additional-properties
```