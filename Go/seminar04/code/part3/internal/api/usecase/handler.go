package usecase

import (
	"context"
	"encoding/json"
	"net/http"

	"go.uber.org/zap"
)

type Repository interface {
	Get(ctx context.Context, key string) (string, error)
	Set(ctx context.Context, key string, value string) error
}

type GetHandler struct {
	repository Repository
	log        *zap.Logger
}

func NewGetHandler(repository Repository, log *zap.Logger) GetHandler {
	return GetHandler{
		repository: repository,
		log:        log,
	}
}

func (h GetHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	ctx, key := r.Context(), r.PathValue("key")

	value, err := h.repository.Get(ctx, key)
	h.log.Info(
		"called GetHandler",
		zap.String("key", key),
		zap.String("result", value),
		zap.Error(err),
	)

	if err != nil {
		responseError(w, err)

		return
	}

	responseSuccess(w, value)
}

type SetHandler struct {
	repository Repository
	log        *zap.Logger
}

func NewSetHandler(repository Repository, log *zap.Logger) SetHandler {
	return SetHandler{
		repository: repository,
		log:        log,
	}
}

func (h SetHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	ctx, key, value := r.Context(), r.PathValue("key"), r.PathValue("value")
	err := h.repository.Set(ctx, key, value)

	h.log.Info(
		"called SetHandler",
		zap.String("key", key),
		zap.String("value", value),
		zap.Error(err),
	)

	if err != nil {
		responseError(w, err)

		return
	}

	responseSuccess(w, true)
}

func responseError(w http.ResponseWriter, err error) {
	type Dto struct {
		Error string `json:"error"`
	}

	resp := Dto{Error: err.Error()}
	respBody, _ := json.Marshal(resp)

	_, _ = w.Write(respBody)
}

func responseSuccess(w http.ResponseWriter, result any) {
	type Dto struct {
		Result any `json:"result"`
	}

	resp := Dto{Result: result}
	respBody, _ := json.Marshal(resp)

	_, _ = w.Write(respBody)
}
