#!/usr/bin/env python

from confluent_kafka import Producer
from confluent_kafka.serialization import SerializationContext, MessageField
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.json_schema import JSONSerializer

from assets.materials.src.temp_data import schema_str, temperature_data
from kafka_utils import KAFKA_CONFIG, SR_CONFIG, TEMP_TOPIC, delivery_callback


def temp_to_dict(temp, ctx):
    return {
        "city": temp.city,
        "reading": temp.reading,
        "unit": temp.unit,
        "timestamp": temp.timestamp,
        # "test": "schema",
    }


if __name__ == '__main__':
    # Создается экземпляр SchemaRegistryClient
    schema_registry_client = SchemaRegistryClient(SR_CONFIG)
    # Создается экземпляр JSONSerializer
    json_serializer = JSONSerializer(
        schema_str,
        schema_registry_client,
        temp_to_dict,
    )
    # Создается экземпляр Producer
    producer = Producer(KAFKA_CONFIG)

    # Создание данные
    for temp in temperature_data:
        producer.produce(
            topic=TEMP_TOPIC,
            key=str(temp.city),
            value=json_serializer(
                temp,
                SerializationContext(TEMP_TOPIC, MessageField.VALUE)
            ),
            on_delivery=delivery_callback
        )

    producer.flush()
