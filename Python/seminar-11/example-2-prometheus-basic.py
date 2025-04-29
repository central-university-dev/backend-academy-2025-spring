import random
import time

from flask import Flask, Response, jsonify, request
from prometheus_client import (
    CONTENT_TYPE_LATEST,
    Counter,
    Gauge,
    Histogram,
    generate_latest,
)

app = Flask(__name__)

REQUEST_COUNT = Counter(
    "http_requests_total",
    "Total HTTP-requests",
    ["method", "endpoint", "http_status"],
)

REQUEST_LATENCY = Histogram(
    "http_request_latency_seconds",
    "Request latentcy",
    ["endpoint"],
    buckets=(0.05, 0.1, 0.25, 0.5, 1, 2, 5),
)

IN_PROGRESS = Gauge(
    "inprogress_requests",
    "In progress",
)


@app.before_request
def before():
    request.start_time = time.time()
    IN_PROGRESS.inc()


@app.after_request
def after(response):
    latency = time.time() - request.start_time
    REQUEST_LATENCY.labels(request.path).observe(latency)
    REQUEST_COUNT.labels(request.method, request.path, response.status_code).inc()
    IN_PROGRESS.dec()
    return response


@app.route("/")
def index():
    return jsonify(message="hello world")


@app.route("/items/<int:item_id>")
def get_item(item_id: int):
    time.sleep(random.random() / 10)
    if random.random() < 0.2:
        return jsonify(error="not found"), 404
    return jsonify(id=item_id, value=random.random())


@app.route("/metrics")
def metrics():
    return Response(generate_latest(), mimetype=CONTENT_TYPE_LATEST)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
