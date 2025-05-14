package usecase

import (
	"context"
	"net/http"

	"github.com/labstack/echo/v4"
)

type Repository interface {
	Get(ctx context.Context, key string) (string, error)
}

type Handler struct {
	repository Repository
}

func NewHandler(repository Repository) Handler {
	return Handler{
		repository: repository,
	}
}

func (h Handler) Get(c echo.Context) error {
	itemId := c.Param("item-id")
	value, err := h.repository.Get(c.Request().Context(), itemId)

	if err != nil {
		return c.JSON(
			http.StatusInternalServerError,
			map[string]string{"error": err.Error()},
		)
	}

	return c.JSON(
		http.StatusOK,
		map[string]string{"value": value},
	)
}
