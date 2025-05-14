package usecase

import (
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"go.opentelemetry.io/contrib/instrumentation/github.com/labstack/echo/otelecho"

	"example.com/seminar14/app02/internal/config"
)

func NewRouter(cfg *config.Config, handler Handler) *echo.Echo {
	e := echo.New()

	e.Use(middleware.Logger())
	e.Use(middleware.Recover())
	e.Use(otelecho.Middleware(cfg.Tracing.ServiceName))

	e.GET("/api/cache/:item-id/", handler.Get)

	return e
}
