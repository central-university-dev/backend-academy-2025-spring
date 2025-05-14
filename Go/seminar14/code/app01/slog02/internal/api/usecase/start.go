package usecase

import (
	"errors"
	"fmt"
	"net/http"

	"github.com/labstack/echo/v4"

	"example.com/seminar14/app01/internal/config"
)

func Start(cfg config.Listen, server *echo.Echo) error {
	if err := server.Start(fmt.Sprintf(":%d", cfg.Port)); err != nil && !errors.Is(err, http.ErrServerClosed) {
		return fmt.Errorf("failed to start server: %w", err)
	}

	return nil
}
