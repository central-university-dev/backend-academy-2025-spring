package usecase

import (
	"context"
	"net/http"

	"github.com/labstack/echo/v4"
	"go.uber.org/zap"
)

type SlowStorage interface {
	GetValueForItem(ctx context.Context, itemId string) (int, error)
}

type Cache interface {
	Get(itemId string) (int, bool)
	Add(key string, value int) (evicted bool)
}

type Handler struct {
	log         *zap.Logger
	slowStorage SlowStorage
	cache       Cache
}

func NewHandler(log *zap.Logger, slowStorage SlowStorage, cache Cache) Handler {
	return Handler{log, slowStorage, cache}
}

func (h Handler) Get(c echo.Context) error {
	type ResponseSuccess struct {
		Value     int  `json:"value"`
		FromCache bool `json:"from_cache"`
	}

	itemId := c.Param("item-id")
	log := h.log.With(zap.String("item_id", itemId))

	log.Debug("received GET request")

	valueFromCache, exists := h.cache.Get(itemId)
	if exists {
		log.Debug("cache hit", zap.Int("value", valueFromCache))

		return c.JSON(
			http.StatusOK,
			ResponseSuccess{
				Value:     valueFromCache,
				FromCache: true,
			},
		)
	}
	log.Debug("cache miss")

	ctx := c.Request().Context()
	valueFromSlowStorage, err := h.slowStorage.GetValueForItem(ctx, itemId)
	if err != nil {
		log.Error("failed to query slow storage", zap.Error(err))

		type ResponseError struct {
			Error string `json:"error"`
		}

		return c.JSON(
			http.StatusBadGateway,
			ResponseError{Error: err.Error()},
		)
	}
	h.cache.Add(itemId, valueFromSlowStorage)

	return c.JSON(
		http.StatusOK,
		ResponseSuccess{
			Value:     valueFromSlowStorage,
			FromCache: false,
		},
	)
}
