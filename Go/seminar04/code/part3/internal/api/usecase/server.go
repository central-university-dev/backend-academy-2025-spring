package usecase

import (
	"context"
	"fmt"
	"net"
	"net/http"

	"go.uber.org/fx"

	"example.com/seminar04/part3/internal/config"
)

func NewServer(cfg config.Listen, mux *http.ServeMux, lc fx.Lifecycle) *http.Server {
	addr := fmt.Sprintf(":%d", cfg.Port)
	srv := &http.Server{
		Addr:    addr,
		Handler: mux,
	}

	lc.Append(fx.Hook{
		OnStart: func(ctx context.Context) error {
			listener, err := net.Listen(cfg.Network, addr)
			if err != nil {
				return fmt.Errorf("failed to start listener on %s: %w", addr, err)
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

func TouchServer(_ *http.Server) {}
