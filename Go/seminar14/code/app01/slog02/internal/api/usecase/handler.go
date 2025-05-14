package usecase

import (
	"context"
	"log/slog"
	"net/http"

	"github.com/labstack/echo/v4"
	slogctx "github.com/veqryn/slog-context"
)

type Handler struct {
	log *slog.Logger
}

func NewHandler(log *slog.Logger) Handler {
	return Handler{log}
}

func (h Handler) Get(c echo.Context) error {
	orderId := c.Param("order-id")

	ctx := c.Request().Context()
	log := slogctx.FromCtx(ctx).With(slog.String("order_id", orderId))
	logCtx := slogctx.NewCtx(ctx, log)

	order, err := h.getOrderBy(logCtx, orderId)
	if err != nil {
		return c.JSON(
			http.StatusInternalServerError,
			map[string]string{"error": err.Error()},
		)
	}

	return c.JSON(
		http.StatusOK,
		order,
	)
}

type Order struct {
	Id          string `json:"id"`
	Description string `json:"description"`
}

func (h Handler) getOrderBy(ctx context.Context, orderId string) (*Order, error) {
	log := slogctx.FromCtx(ctx)
	log.Info("getting order by id")

	return &Order{
		Id:          orderId,
		Description: "some order",
	}, nil
}
