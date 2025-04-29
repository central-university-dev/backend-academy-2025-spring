from time import sleep, time
from random import random, choice
from prometheus_client import (
    Counter,
    Gauge,
    Histogram,
    Summary,
    start_http_server,
)

REQUEST_COUNT = Counter("request_total", "Total HTTP-requests", ["method", "endpoint"])
IN_PROGRESS = Gauge("in_progress", "In progress")
REQ_LATENCY = Histogram(
    "request_latency_seconds",
    "Request latentcy",
    buckets=(0.05, 0.1, 0.25, 0.5, 1, 2, 5),
)
REQ_LATENCY_Q = Summary("request_latency_quantiles", "Quantilies summary")

METHODS = ["GET", "POST"]
ENDPOINTS = ["/", "/login", "/api/items"]

if __name__ == "__main__":
    start_http_server(8000)
    while True:
        method = choice(METHODS)
        endpoint = choice(ENDPOINTS)
        with IN_PROGRESS.track_inprogress():
            start = time()
            sleep(random() / 10)
            REQUEST_COUNT.labels(method, endpoint).inc()
            duration = time() - start
            REQ_LATENCY.observe(duration)
            REQ_LATENCY_Q.observe(duration)
