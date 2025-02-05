package main

import (
	"context"
	"fmt"
	"net"
	"net/http"

	"go.uber.org/fx"
)

func NewServer(cfg Config, mux *http.ServeMux, lc fx.Lifecycle) *http.Server {
	srv := &http.Server{
		Addr:    cfg.Address,
		Handler: mux,
	}

	lc.Append(fx.Hook{
		OnStart: func(ctx context.Context) error {
			listener, err := net.Listen("tcp", cfg.Address)
			if err != nil {
				return fmt.Errorf("failed to start listener on %s: %w", cfg.Address, err)
			}

			go srv.Serve(listener)

			return nil
		},
		OnStop: func(ctx context.Context) error {
			if err := srv.Shutdown(ctx); err != nil {
				return fmt.Errorf("failed to shutdown server: %w", err)
			}

			return nil
		},
	})

	return srv
}
