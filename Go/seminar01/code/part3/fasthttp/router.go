package main

import (
	"github.com/fasthttp/router"
)

func NewRouter(echoHandler *EchoHandler, helloHandler *HelloHandler) *router.Router {
	r := router.New()
	r.POST("/echo/", echoHandler.HandleFastHTTP)
	r.GET("/hello/{name}/", helloHandler.HandleFastHTTP)
	return r
}
