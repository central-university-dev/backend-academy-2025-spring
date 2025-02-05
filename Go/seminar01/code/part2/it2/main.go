package main

import (
	"net/http"

	"go.uber.org/fx"
)

func main() {
	appOpts := fx.Options(
		fx.Provide(NewConfig),
		fx.Provide(NewLogger),
		fx.Provide(NewEchoHandler),
		fx.Provide(NewMux),
		fx.Provide(NewServer),
		fx.Invoke(func(*http.Server) {}),
	)

	fx.New(appOpts).Run()
}
