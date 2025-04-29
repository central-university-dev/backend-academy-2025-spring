from fastapi import FastAPI
from starlette.responses import Response
from prometheus_client import generate_latest, CONTENT_TYPE_LATEST

from opentelemetry import metrics
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.exporter.prometheus import PrometheusMetricReader

reader = PrometheusMetricReader()
metrics.set_meter_provider(MeterProvider(metric_readers=[reader]))
meter = metrics.get_meter(__name__)

req_counter = meter.create_counter(
    "http_requests_total",
    description="Total HTTP-requests",
)

app = FastAPI(title="Ping-service with OTEL metrics")


@app.get("/ping")
async def ping():
    req_counter.add(1, {"endpoint": "/ping"})
    return {"pong": True}


@app.get("/metrics")
async def metrics_endpoint():
    data = (
        generate_latest()
    )  # PrometheusMetricReader has pushed all metrics here already
    return Response(data, media_type=CONTENT_TYPE_LATEST)
