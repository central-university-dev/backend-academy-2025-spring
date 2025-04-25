"""
docker run -p 9091:9091 prom/pushgateway
"""

import random
from prometheus_client import CollectorRegistry, Gauge, push_to_gateway

registry = CollectorRegistry()
TEMPERATURE = Gauge(
    "kettle_temperature_celsius",
    "Kettle temperature",
    registry=registry,
)


def main():
    for _ in range(5):
        TEMPERATURE.set(random.uniform(20, 100))
        push_to_gateway(
            "localhost:9091",
            job="kettle",
            grouping_key={"room": "kitchen"},
            registry=registry,
        )


if __name__ == "__main__":
    main()
