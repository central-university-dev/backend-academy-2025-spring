package main

import (
	"github.com/valyala/fasthttp"
	"go.uber.org/fx"
)

func main() {
	appOpts := fx.Options(
		fx.Provide(NewConfig),
		fx.Provide(NewLogger),
		fx.Provide(NewEchoHandler),
		fx.Provide(NewHelloHandler),
		fx.Provide(NewRouter),
		fx.Provide(NewServer),
		fx.Invoke(func(*fasthttp.Server) {}),
	)

	fx.New(appOpts).Run()
}
