from time import sleep
from random import randint

from prometheus_client import start_http_server
from opentelemetry import metrics
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.exporter.prometheus import PrometheusMetricReader

start_http_server(port=9464, addr="0.0.0.0")

reader = PrometheusMetricReader()
metrics.set_meter_provider(MeterProvider(metric_readers=[reader]))
meter = metrics.get_meter(__name__)

req_counter = meter.create_counter(
    "http_requests_total", description="Всего HTTP-запросов"
)
active_sessions = meter.create_up_down_counter(
    "active_sessions", description="Счётчик с +1/-1"
)
histogram = meter.create_histogram(
    "payload_size_bytes", description="Размер входящих сообщений"
)

print("Metrics at :9464/metrics …")
while True:
    size = randint(200, 2_000)
    histogram.record(size, {"endpoint": "/upload"})
    req_counter.add(1, {"endpoint": "/upload"})
    active_sessions.add(1)
    sleep(0.1)
    active_sessions.add(-1)
