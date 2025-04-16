#!/usr/bin/env python

from time import sleep, time, strftime, localtime

from faker import Faker

from confluent_kafka import Producer
from confluent_kafka.serialization import SerializationContext, MessageField
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer

from kafka_utils import KAFKA_CONFIG, SR_CONFIG, AVRO_TOPIC

avro_schema = """{
  "type": "record",
  "name": "application_2",
  "namespace": "com.tink_2",
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

fake = Faker('ru_RU')

if __name__ == '__main__':
    # Создается экземпляр SchemaRegistryClient
    schema_registry_client = SchemaRegistryClient(SR_CONFIG)
    # Создается экземпляр JSONSerializer
    avro_serializer = AvroSerializer(
        schema_registry_client,
        avro_schema,
    )
    # Создается экземпляр Producer
    producer = Producer(KAFKA_CONFIG)
    AVRO_TOPIC += '_new'

    id_ = 0
    # Бесконечный цикл публикации данных.
    while True:
        start_time = time()
        id_ += 1
        producer_publish_time = strftime("%d.%m.%Y %H:%M:%S", localtime(start_time))
        application = {
            "id": id_,
            "event_time": producer_publish_time,
            "client": fake.name(),
            "email": fake.free_email(),
            "phone": fake.phone_number(),
        }
        value = avro_serializer(application, SerializationContext(AVRO_TOPIC, MessageField.VALUE))
        future = producer.produce(topic=AVRO_TOPIC, value=value)
        print("Message sent successfully")
        print(f' [*] Payload {application}')
        # Повтор через 5 секунды.
        sleep(5)
