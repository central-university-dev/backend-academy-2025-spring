package tracing

import (
	"example.com/seminar14/app02/internal/config"
	tracesdk "go.opentelemetry.io/otel/sdk/trace"
	"go.opentelemetry.io/otel/trace"
)

func NewTracer(cfg config.Tracing, traceProvider *tracesdk.TracerProvider) trace.Tracer {
	return traceProvider.Tracer(cfg.MainTracerName)
}
