- [http клиент](#http-клиент)
  - [Простой клиент](#простой-клиент)
  - [Транспорт](#транспорт)
  - [Трассировка запроса](#трассировка-запроса)
  - [resty](#resty)

# http клиент

## Простой клиент

Хотя пакет `net/http` содержит в себе такие функции, как `http.Get` и `http.Post`, лучше их не использовать, а инициализировать собственный клиент. Причина этого в том, что эти функции используют `http.DefaultClient`, у которого не установлен таймаут. Это означает, что любой запрос выполняется без таймаута, и есть вероятность, что он никогда не завершится. 

Пользоваться клиентом и совершать запросы достаточно просто. В качестве примера, запустим http-сервер из прошлого раздела, и попытаемся отправить на него GET и POST запросы.

GET запрос:

```go
package main

import (
    "fmt"
    "io"
    "net/http"
    "time"
)

func NewClient() *http.Client {
    const timeout = 1 * time.Second

    return &http.Client{Timeout: timeout}
}

func BuildRequest() (*http.Request, error) {
    return http.NewRequest("GET", "http://127.0.0.1:8080/hello/there/", nil)
}

func main() {
    client := NewClient()
    req, err := BuildRequest()
    if err != nil {
        fmt.Println("failed to build request", err)

        return
    }

    resp, err := client.Do(req)
    if err != nil {
        fmt.Println("failed to do request", err)

        return
    }
    fmt.Println("response status:", resp.Status)

    respBody, err := io.ReadAll(resp.Body)
    if err != nil {
        fmt.Println("failed to read response body", err)

        return
    }
    fmt.Printf("response body: %s\n", respBody)
}
```

Результат запуска:
```
response status: 200 OK
response body: Hello, there!
```

Если уменьшить таймаут запроса до 1мкс, то результат запроса будет такой:
```
failed to do request Get "http://127.0.0.1:8080/hello/there/": context deadline exceeded (Client.Timeout exceeded while awaiting headers)
```

POST запрос:

```go
package main

import (
    "fmt"
    "io"
    "net/http"
    "strings"
    "time"
)

func NewClient() *http.Client {
    const timeout = 1 * time.Second

    return &http.Client{Timeout: timeout}
}

func BuildRequest() (*http.Request, error) {
    requestBody := "some data"

    return http.NewRequest("POST", "http://127.0.0.1:8080/echo/", strings.NewReader(requestBody))
}

func main() {
    client := NewClient()
    req, err := BuildRequest()
    if err != nil {
        fmt.Println("failed to build request", err)

        return
    }

    resp, err := client.Do(req)
    if err != nil {
        fmt.Println("failed to do request", err)

        return
    }
    fmt.Println("response status:", resp.Status)

    respBody, err := io.ReadAll(resp.Body)
    if err != nil {
        fmt.Println("failed to read response body", err)

        return
    }
    fmt.Printf("response body: %s\n", respBody)
}
```

Результат запуска:
```
response status: 200 OK
response body: some data
```

Если поменять метод запроса с POST на GET, то результат будет таким:
```
response status: 405 Method Not Allowed
response body: Method Not Allowed
```

## Транспорт

В http.Client есть поле `Transport` с типом `RoundTripper`:

```go
type RoundTripper interface {
    RoundTrip(*Request) (*Response, error)
}
```

Если транспорт не назначен, то по умолчанию применяется стандартный транспорт:

```go
var DefaultTransport RoundTripper = &Transport{
    Proxy: ProxyFromEnvironment,
    DialContext: defaultTransportDialContext(&net.Dialer{
        Timeout:   30 * time.Second,
        KeepAlive: 30 * time.Second,
    }),
    ForceAttemptHTTP2:     true,
    MaxIdleConns:          100,
    IdleConnTimeout:       90 * time.Second,
    TLSHandshakeTimeout:   10 * time.Second,
    ExpectContinueTimeout: 1 * time.Second,
}
```

Транспорт определяет различные настройки, такие как конфигурация tls, общее количество и время жизни бездействующих подключений (MaxIdleConns, MaxIdleConnsPerHost, IdleConnTimeout), настройки прокси и пр.

Важное замечание: транспорт нужно не создавать отдельно на каждый запрос или на каждого клиента, а переиспользовать. Стандартный `http.Transport` потокобезопасен.

## Трассировка запроса

Есть возможность получить дополнительные данные о тех этапах, которые используются при выполнении запроса. Для этого, в контекст запроса нужно добавить объект `*httptrace.ClientTrace`. В примере ниже пишутся в лог разные этапы выполнения запроса:

```go
package main

import (
    "fmt"
    "io"
    "log/slog"
    "net/http"
    "net/http/httptrace"
    "time"
)

func NewClient() *http.Client {
    const timeout = 1 * time.Second

    return &http.Client{Timeout: timeout}
}

func BuildRequest() (*http.Request, error) {
    trace := &httptrace.ClientTrace{
        GetConn: func(hostPort string) {
            slog.Info("GetConn", slog.String("hostPort", hostPort))
        },
        GotConn: func(info httptrace.GotConnInfo) {
            slog.Info(
                "GotConn",
                slog.Bool("reused", info.Reused),
                slog.Bool("wasIdle", info.WasIdle),
            )
        },
        PutIdleConn: func(err error) {
            slog.Info("PutIdleConn")
        },
        GotFirstResponseByte: func() {
            slog.Info("GotFirstResponseByte")
        },
        DNSStart: func(info httptrace.DNSStartInfo) {
            slog.Info("DNSStart", slog.String("host", info.Host))
		},
		DNSDone: func(info httptrace.DNSDoneInfo) {
			slog.Info("DNSDone")
        },
        ConnectStart: func(network, addr string) {
            slog.Info(
                "ConnectStart",
                slog.String("host", addr),
                slog.String("network", network),
            )
        },
        ConnectDone: func(network, addr string, err error) {
            slog.Info(
                "ConnectDone",
                slog.String("host", addr),
                slog.String("network", network),
            )
        },
        WroteHeaderField: func(key string, value []string) {
            slog.Info(
                "WroteHeaderField",
                slog.String("key", key),
                slog.Any("vals", value),
            )
        },
        WroteHeaders: func() {
            slog.Info("WroteHeaders")
        },
        WroteRequest: func(info httptrace.WroteRequestInfo) {
            slog.Info("WroteRequest")
        },
    }

    req, err := http.NewRequest("GET", "http://127.0.0.1:8080/hello/there/", nil)
    if err != nil {
        return nil, err
    }

    return req.WithContext(httptrace.WithClientTrace(req.Context(), trace)), nil
}

func main() {
    client := NewClient()
    req, err := BuildRequest()
    if err != nil {
        fmt.Println("failed to build request", err)

        return
    }

    resp, err := client.Do(req)
    if err != nil {
        fmt.Println("failed to do request", err)

        return
    }
    fmt.Println("response status:", resp.Status)

    respBody, err := io.ReadAll(resp.Body)
    if err != nil {
        fmt.Println("failed to read response body", err)

        return
    }
    fmt.Printf("response body: %s\n", respBody)
}
```

Результат выполнения:

```
2025/02/05 15:13:50 INFO GetConn hostPort=127.0.0.1:8080
2025/02/05 15:13:50 INFO ConnectStart host=127.0.0.1:8080 network=tcp
2025/02/05 15:13:50 INFO ConnectDone host=127.0.0.1:8080 network=tcp
2025/02/05 15:13:50 INFO GotConn reused=false wasIdle=false
2025/02/05 15:13:50 INFO WroteHeaderField key=Host vals=[127.0.0.1:8080]
2025/02/05 15:13:50 INFO WroteHeaderField key=User-Agent vals=[Go-http-client/1.1]
2025/02/05 15:13:50 INFO WroteHeaderField key=Accept-Encoding vals=[gzip]
2025/02/05 15:13:50 INFO WroteHeaders
2025/02/05 15:13:50 INFO WroteRequest
2025/02/05 15:13:50 INFO GotFirstResponseByte
response status: 200 OK
2025/02/05 15:13:50 INFO PutIdleConn
response body: Hello, there!
```

В функции `httptrace.ClientTrace` можно добавить измерение времени выполнения. Это поможет детально понимать, на какие этапы выполнения запроса тратится больше всего времени.

## resty

Выполнение http-запросов с помощью `net/http` включает в себя достаточно много boilerplate кода: создание клиента, создание запроса, чтение тела запроса, проверка ошибок на этих этапах. Существует множество пакетов, которые упрощают выполнение запросов в golang. Рассмотрим один из таких пакетов, `github.com/go-resty/resty`. [Пример кода](../code/part4/resty).

В `resty` поддерживается много разных функций, включая кодирование и декодирование тела запроса. [Документация](https://resty.dev/docs/response-auto-parse/).

Трассировка этапов запроса тоже [поддерживается](https://resty.dev/docs/request-tracing/).
