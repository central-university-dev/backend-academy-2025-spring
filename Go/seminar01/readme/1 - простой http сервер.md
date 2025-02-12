- [Простой http сервер](#простой-http-сервер)
  - [http.Handler и http.HandlerFunc](#httphandler-и-httphandlerfunc)
  - [ListenAndServe](#listenandserve)
  - [Мультиплексирование запросов](#мультиплексирование-запросов)
  - [Паника в обработчике](#паника-в-обработчике)

# Простой http сервер

## http.Handler и http.HandlerFunc

Код, который обрабатывает http-запрос и формирует http-ответ, должен соответствовать сигнатуре `http.Handler`:

```go
type Handler interface {
    ServeHTTP(w ResponseWriter, r *Request)
}
```

Этот интерфейс требует, чтобы была объявлена всего одна функция. Чтобы не определять отдельный новый тип для каждого обработчика, можно воспользоваться типом `http.HandlerFunc`, который преобразует функцию с подходящей сигнатурой в `http.Handler`:

```go
// The HandlerFunc type is an adapter to allow the use of
// ordinary functions as HTTP handlers. If f is a function
// with the appropriate signature, HandlerFunc(f) is a
// [Handler] that calls f.
type HandlerFunc func(ResponseWriter, *Request)

// ServeHTTP calls f(w, r).
func (f HandlerFunc) ServeHTTP(w ResponseWriter, r *Request) {
    f(w, r)
}
```

## ListenAndServe

Функция `http.ListenAndServe` запускает http-сервер, который слушает (Listen) входящие TCP-запросы по определённому адресу, и вызывает для них подходящие обработчики (`http.Handler`), которые обслуживают (Serve) входящие запросы. 

Запустим сервер, который будет выполнять простейшую обработку запросов (на любой запрос возвращать текстовый ответ `Hello`):

```go
package main

import (
    "fmt"
    "net/http"
)

func SimpleHandler(w http.ResponseWriter, _ *http.Request) {
    w.WriteHeader(http.StatusOK)
    if _, err := w.Write([]byte("Hello\n")); err != nil {
        panic(fmt.Errorf("could not write response: %w", err))
    }
}

func main() {
    http.Handle("/", http.HandlerFunc(SimpleHandler))
    err := http.ListenAndServe(":8080", nil)

    if err != nil {
        fmt.Println("server failed to start or finished with error", err)
    } else {
        fmt.Println("application stopped")
    }
}
```

С помощью curl проверим обработку запросов:


```shell
$ curl http://localhost:8080/
Hello

$ curl http://127.0.0.1:8080/
Hello

$ curl http://127.0.0.1:8080/random/path/
Hello

$ curl -X POST http://0.0.0.0:8080/
Hello
```

Видно, что сервер передаёт указанный ответ. Можно сделать выводы:

1. В качестве имени хоста успешно принимаются как `localhost`, так и `127.0.0.1`, и `0.0.0.0`. Разницу между этими тремя адресами предлагается изучить самостоятельно. Начать можно со [Stack Overflow](https://stackoverflow.com/questions/20778771/what-is-the-difference-between-0-0-0-0-127-0-0-1-and-localhost).
2. Обработчик вызывается не только при запросе на "корневой" URL, но и при запросе на другие адреса (`/random/path/`).
3. Обработчик вызывается не только при запросе с методом `GET`, но и при запросе с методом `POST`.
4. Непонятен способ завершить приложение штатным образом. `ctrl+C` завершает приложение, но аварийно.

## Мультиплексирование запросов

Разберём подробнее строки кода из примера выше:

```go
http.Handle("/", http.HandlerFunc(SimpleHandler))
err := http.ListenAndServe(":8080", nil)
```

В этом коде в функцию `http.ListenAndServe` передаётся `nil` вторым параметром. Это означает, что будет использован обработчик, зарегистрированный глобально (`http.Handle("/", http.HandlerFunc(SimpleHandler))`).

Обработчик фактически является зависимостью сервера, и правильнее передавать её явно при инициализации. Для этого используется `*http.ServeMux`, который умеет выбирать нужный обработчик в зависимости от разных параметров запроса. Код из примера выше можно переписать с помощью `ServeMux`:


```go
package main

import (
    "fmt"
    "net/http"
)

func SimpleHandler(w http.ResponseWriter, _ *http.Request) {
    w.WriteHeader(http.StatusOK)
    if _, err := w.Write([]byte("Hello\n")); err != nil {
        panic(fmt.Errorf("could not write response: %w", err))
    }
}

func InitRouting() *http.ServeMux {
    mux := http.NewServeMux()
    mux.HandleFunc("/", SimpleHandler) // или mux.Handle("/", http.HandlerFunc(SimpleHandler))
    return mux
}

func InitServer(addr string, handler http.Handler) *http.Server {
    return &http.Server{
        Addr:    addr,
        Handler: handler,
    }
}

func main() {
    const addr = ":8080"
    server := InitServer(addr, InitRouting())

    if err := server.ListenAndServe(); err != nil {
        fmt.Println("server failed to start or finished with error", err)
    } else {
        fmt.Println("application stopped")
    }
}
```

Мультиплексор позволяет гибко настроить маршрутизацию обработчиков запроса. Пример с немного более сложной маршрутизацией (обработка ошибок опущена):

```go
package main

import (
    "fmt"
    "net/http"
)

type UserHandler struct{}

func (UserHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
    _, _ = w.Write([]byte("Hello from UserHandler\n"))
}

type SpecificHandler struct{}

func (SpecificHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
    _, _ = fmt.Fprintf(w, "Hello from SpecificHandler (id = %s)\n", r.PathValue("id"))
}

func InitRouting() *http.ServeMux {
    mux := http.NewServeMux()
    mux.Handle("/users/all/", UserHandler{})
    mux.Handle("POST /some/entity/{id}/", SpecificHandler{})
    return mux
}

func InitServer(addr string, handler http.Handler) *http.Server {
    return &http.Server{
        Addr:    addr,
        Handler: handler,
    }
}

func main() {
    const addr = ":8080"
    server := InitServer(addr, InitRouting())

    if err := server.ListenAndServe(); err != nil {
        fmt.Println("server failed to start or finished with error", err)
    } else {
        fmt.Println("application stopped")
    }
}
```

Результаты нескольких разных запросов:

```shell
# обработчика для "/" больше нет
$ curl http://localhost:8080/
404 page not found

# нет шаблона, который соответствовал бы "/random/path/"
$ curl http://localhost:8080/random/path/
404 page not found

# вызван обработчик UserHandler
$ curl http://localhost:8080/users/all/ 
Hello from UserHandler

# для метода POST вызывается тот же обработчик
$ curl -X POST http://localhost:8080/users/all/ 
Hello from UserHandler

# обработчик есть только для метода POST
# поэтому на запрос с методом GET 
# возвращается ответ с кодом состояния 405
$ curl http://localhost:8080/some/entity/123/
Method Not Allowed

# вызван обработчик SpecificHandler
$ curl -X POST http://localhost:8080/some/entity/456/
Hello from SpecificHandler (id = 456)
```

Более подробно про особенности работы шаблонов рассказывается в статьях:

1. https://habr.com/ru/companies/avito/articles/805097/ (рус.)
2. https://shijuvar.medium.com/building-rest-apis-with-go-1-22-http-servemux-2115f242f02b (англ.)

## Паника в обработчике

Если код обработчика вызовет панику, то `ListenAndServe` восстановится и продолжит работу. Так сделано для того, чтобы проблемы с одним запросом не влияли на возможность обслуживать остальные:

```go
package main

import (
  "fmt"
  "net/http"
)

func NormalHandler(w http.ResponseWriter, _ *http.Request) {
  _, _ = w.Write([]byte("Hello from NormalHandler\n"))
}

func PanicHandler(w http.ResponseWriter, _ *http.Request) {
  panic("PanicHandler panicked")
}

func InitRouting() *http.ServeMux {
  mux := http.NewServeMux()
  mux.HandleFunc("/ok/", NormalHandler)
  mux.HandleFunc("/panic/", PanicHandler)
  return mux
}

func InitServer(addr string, handler http.Handler) *http.Server {
  return &http.Server{
    Addr:    addr,
    Handler: handler,
  }
}

func main() {
  const addr = ":8080"
  server := InitServer(addr, InitRouting())

  if err := server.ListenAndServe(); err != nil {
    fmt.Println("server failed to start or finished with error", err)
  } else {
    fmt.Println("application stopped")
  }
}
```

Вот что будет, если запустить это приложение:

```shell
# сервер при этом выведет в лог сообщение о панике
$ curl http://127.0.0.1:8080/panic/      
curl: (52) Empty reply from server

# следующий запрос успешно выполнится
$ curl http://127.0.0.1:8080/ok/   
Hello from NormalHandler
```
