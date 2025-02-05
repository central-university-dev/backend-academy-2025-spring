package main

import (
	"context"
	"fmt"
	"net"

	"github.com/fasthttp/router"
	"github.com/valyala/fasthttp"
	"go.uber.org/fx"
)

func NewServer(cfg Config, router *router.Router, lc fx.Lifecycle) *fasthttp.Server {
	srv := &fasthttp.Server{
		Handler: router.Handler,
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
			if err := srv.ShutdownWithContext(ctx); err != nil {
				return fmt.Errorf("failed to shutdown server: %w", err)
			}

			return nil
		},
	})

	return srv
}
