package tracing

import (
	"context"

	"go.opentelemetry.io/otel/exporters/otlp/otlptrace"
	tracesdk "go.opentelemetry.io/otel/sdk/trace"
)

func NewSpanExporter(client otlptrace.Client) (tracesdk.SpanExporter, error) {
	return otlptrace.New(context.Background(), client)
}
