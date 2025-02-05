package main

import (
	"net/http"
)

func NewServer(cfg Config, mux *http.ServeMux) *http.Server {
	return &http.Server{
		Addr:    cfg.Address,
		Handler: mux,
	}
}
