package tracing

import (
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/sdk/resource"
	tracesdk "go.opentelemetry.io/otel/sdk/trace"
)

func NewTraceProvider(spanExporter tracesdk.SpanExporter, resource *resource.Resource) *tracesdk.TracerProvider {
	return tracesdk.NewTracerProvider(
		tracesdk.WithBatcher(spanExporter),
		tracesdk.WithResource(resource),
	)
}

func SetDefaultTraceProvider(traceProvider *tracesdk.TracerProvider) {
	otel.SetTracerProvider(traceProvider)
}
