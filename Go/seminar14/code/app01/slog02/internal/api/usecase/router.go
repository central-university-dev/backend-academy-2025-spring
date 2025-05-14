package usecase

import (
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
)

func NewRouter(handler Handler) *echo.Echo {
	e := echo.New()

	e.Use(middleware.Recover())
	e.GET("/api/orders/:order-id/", handler.Get)

	return e
}
