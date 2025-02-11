# ogen

https://github.com/ogen-go/ogen
https://ogen.dev/

## Общие сведения

Фактически в нём реализованы все те же возможности, что и в `oapi-codegen`. Но есть некоторые особенности 
использования. Они описаны в документации, поэтому приведу здесь основные:

- Позволяет расширить OpenAPI своими описаниями (описание возвращаемой ошибки, расширенный набор форматов и т.д.).
- Генерирует интерфейс `Handler`, который необходимо реализовать и передать в `Server` (очень похоже на gRPC генератор кода).
Для каждого метода генерируется структура входных параметров и возвращаемого результата. Генерирует заглушку 
для `Handler` - `UnimplementedHandler`.
- Поддержка трассировки `opentelemetry` на борту.
- Свой интерфейс использования `Middleware`.
- Свои типы данных для валидации nullable полей.

## Установка

```bash
go install -v github.com/ogen-go/ogen/cmd/ogen@latest
```

## Использование

### Вызов command line утилиты
```bash
oapi-codegen -config config.yaml ../oapi/openapi.yaml
```

### Используя go:generate
```go
package generate

//go:generate ogen --target ../../api/ogen --clean ../../oapi/openapi.yaml
```

```go
package generate

//go:generate go run github.com/ogen-go/ogen/cmd/ogen@latest --target ../../api/ogen --clean ../../oapi/openapi.yaml
```