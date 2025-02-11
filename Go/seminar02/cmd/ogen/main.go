package main

import (
	"log"
	"net/http"

	api "example.com/seminar02/api/ogen"
	"github.com/google/uuid"
	"github.com/ogen-go/ogen/middleware"
)

func main() {
	service := &taskManager{
		tasks: make(map[uuid.UUID]api.Task),
	}

	// Create generated server.
	srv, err := api.NewServer(service, &Auth{PIMPApiKey}, api.WithMiddleware(mw, parametrizedMw(5)))
	if err != nil {
		log.Fatal(err)
	}

	if err := http.ListenAndServe("localhost:8080", srv); err != nil {
		log.Fatal(err)
	}
}

func mw(req middleware.Request, next middleware.Next) (middleware.Response, error) {
	log.Println("Roll in the Benz with me, you could watch TV")
	return next(req)
}

func parametrizedMw(cutoff int64) api.Middleware {
	return func(req middleware.Request, next middleware.Next) (middleware.Response, error) {
		if req.Raw.ContentLength%10 > cutoff {
			log.Println("From the backseat of my V, I'm a P.I.M.P")
		}

		return next(req)
	}
}
