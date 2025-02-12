package main

import (
	"io"
	"net/http"

	"go.uber.org/zap"
)

type EchoHandler struct {
	log *zap.Logger
}

func NewEchoHandler(log *zap.Logger) *EchoHandler {
	return &EchoHandler{log: log}
}

func (h *EchoHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	_, writeErr := io.Copy(w, r.Body)
	if writeErr != nil {
		h.log.Error("failed to write response", zap.Error(writeErr))

		return
	}

	h.log.Info("handler called successfully")
}
