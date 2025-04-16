#!/usr/bin/env python
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.serialization import SerializationContext, MessageField
from confluent_kafka.schema_registry.avro import AvroDeserializer
from confluent_kafka import Consumer, OFFSET_BEGINNING

from kafka_utils import CONSUMER_CONFIG, AVRO_TOPIC, SR_CONFIG

avro_schema = """{
  "type": "record",
  "name": "application",
  "namespace": "com.tink",
  "fields": [
    {
      "name": "id",
      "type": "long"
    },
    {
      "name": "event_time",
      "type": "string"
    },
    {
      "name": "client",
      "type": "string"
    },
    {
      "name": "email",
      "type": "string"
    },
    {
      "name": "phone",
      "type": "string"
    }
  ]
}
"""


def reset_offset(consumer_: Consumer, partitions, reset: bool = False):
    if reset:
        for p in partitions:
            p.offset = OFFSET_BEGINNING
        consumer_.assign(partitions)


if __name__ == '__main__':
    schema_registry_client = SchemaRegistryClient(SR_CONFIG)
    # Создаём десериалайзер
    avro_deserializer = AvroDeserializer(schema_registry_client, avro_schema)
    # Создается экземпляр Consumer
    consumer = Consumer(CONSUMER_CONFIG)

    # Подписка на тему
    consumer.subscribe([AVRO_TOPIC], on_assign=reset_offset)

    # Опрос новых сообщений от Кафки и вывод полученного результата в терминал.
    try:
        while True:
            event = consumer.poll(1.0)
            if event is None:
                # Первоначальное потребление сообщения может занять до `session.timeout.ms`,
                # чтобы группа потребителей перебалансировалась и начала потребление
                print('Waiting...')
                continue
            elif event.error():
                print(f'ERROR: {event.error()}')
            data = avro_deserializer(event.value(),
                                     SerializationContext(AVRO_TOPIC, MessageField.VALUE))
            if data is not None:
                print(data)
    except KeyboardInterrupt:
        ...
    finally:
        # Выход из группы и фиксация окончательного смещения
        consumer.close()
