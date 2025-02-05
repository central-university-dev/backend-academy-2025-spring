package main

import (
	"net/http"
)

func NewMux(echoHandler *EchoHandler) *http.ServeMux {
	mux := http.NewServeMux()
	mux.Handle("/echo/", echoHandler)
	return mux
}
