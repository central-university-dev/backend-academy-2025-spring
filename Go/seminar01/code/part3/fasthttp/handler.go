package main

import (
	"fmt"

	"github.com/valyala/fasthttp"
	"go.uber.org/zap"
)

type EchoHandler struct {
	log *zap.Logger
}

func NewEchoHandler(log *zap.Logger) *EchoHandler {
	return &EchoHandler{log: log}
}

func (h *EchoHandler) HandleFastHTTP(ctx *fasthttp.RequestCtx) {
	ctx.SetBody(ctx.PostBody())

	h.log.Info("EchoHandler called successfully")
}

type HelloHandler struct {
	log *zap.Logger
}

func NewHelloHandler(log *zap.Logger) *HelloHandler {
	return &HelloHandler{log: log}
}

func (h *HelloHandler) HandleFastHTTP(ctx *fasthttp.RequestCtx) {
	_, writeErr := fmt.Fprintf(ctx, "Hello, %s!", ctx.UserValue("name"))
	if writeErr != nil {
		h.log.Error("HelloHandler failed to write response", zap.Error(writeErr))

		return
	}

	h.log.Info("HelloHandler called successfully")
}
