- [middleware](#middleware)
  - [На стороне сервера](#на-стороне-сервера)
  - [На стороне клиента](#на-стороне-клиента)

# middleware

## На стороне сервера

Существует возможность выполнять код до и после того, как запрос пройдёт через обработчик. Для этого используется middleware: обычно это функция, которая принимает обработчик в качестве параметра и возвращает новый обработчик. 

Добавим middleware, которая измеряет время обработки запроса на стороне сервера, в код из первой части:

```go
package main

import (
    "fmt"
    "net/http"
    "time"
)

func SimpleHandler(w http.ResponseWriter, _ *http.Request) {
    w.WriteHeader(http.StatusOK)
    _, _ = w.Write([]byte("Hello\n"))
}

func TimingMiddleware(next http.Handler) http.Handler {
    return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        start := time.Now()
        next.ServeHTTP(w, r)
        fmt.Printf("request took %s\n", time.Since(start))
    })
}

func main() {
    http.Handle("/", TimingMiddleware(http.HandlerFunc(SimpleHandler)))
    err := http.ListenAndServe(":8080", nil)

    if err != nil {
        fmt.Println("server failed to start or finished with error", err)
    } else {
        fmt.Println("application stopped")
    }
}
```

Следует обратить внимание на следующее:

- функция `TimingMiddleware` принимает и возвращает `http.Handler`
- нужно не забыть вызвать `next.ServeHTTP(w, r)`!
- `TimingMiddleware` используется для обработки всех запросов: `http.Handle("/", TimingMiddleware(http.HandlerFunc(SimpleHandler)))`
- но можно настроить обработчики таким образом, чтобы отдельные middleware вызывались только для определённых маршрутов, а не для всех запросов

## На стороне клиента

Есть возможность реализовать аналогичную функциональность для клиента. Это делается через "оборачивание" транспорта у http клиента, аналогично тому, как "оборачиваются" обработчики.

Добавим middleware, которая измеряет время обработки запроса на стороне клиента, в код из четвёртой части:

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

    return &http.Client{
        Timeout: timeout,
        Transport: TimingRoundTripper{
            Parent: http.DefaultTransport,
        },
    }
}

func BuildRequest() (*http.Request, error) {
    return http.NewRequest("GET", "http://127.0.0.1:8080/hello/there/", nil)
}

type TimingRoundTripper struct {
	Parent http.RoundTripper
}

func (rt TimingRoundTripper) RoundTrip(req *http.Request) (*http.Response, error) {
    start := time.Now()
    defer func() {
        fmt.Printf("request took %s\n", time.Since(start))
    }()

    return rt.Parent.RoundTrip(req)
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
request took 1.90875ms
response status: 200 OK
response body: Hello, there!
```

`github.com/go-resty/resty` тоже имеет возможность подключать middleware, причём как обрабатывающие запрос, так и ответ:

- https://resty.dev/docs/request-middleware/
- https://resty.dev/docs/response-middleware/
