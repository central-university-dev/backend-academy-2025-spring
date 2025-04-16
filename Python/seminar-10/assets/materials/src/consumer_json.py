#!/usr/bin/env python

from confluent_kafka.serialization import SerializationContext, MessageField
from confluent_kafka.schema_registry.json_schema import JSONDeserializer
from confluent_kafka import Consumer, OFFSET_BEGINNING

from assets.materials.src.temp_data import Temperature, schema_str
from kafka_utils import CONSUMER_CONFIG, TEMP_TOPIC


def dict_to_temp(dict_: dict, ctx):
    return Temperature(
        dict_['city'],
        dict_['reading'],
        dict_['unit'],
        dict_['timestamp'],
    )


def reset_offset(consumer_: Consumer, partitions, reset: bool = False):
    if reset:
        for p in partitions:
            p.offset = OFFSET_BEGINNING
        consumer_.assign(partitions)


if __name__ == '__main__':
    # Создаём десериалайзер
    json_deserializer = JSONDeserializer(schema_str, from_dict=dict_to_temp)
    # Создается экземпляр Consumer
    consumer = Consumer(CONSUMER_CONFIG)

    # Подписка на тему
    consumer.subscribe([TEMP_TOPIC], on_assign=reset_offset)

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
                continue
            temp = json_deserializer(event.value(),
                                     SerializationContext(TEMP_TOPIC, MessageField.VALUE))
            if temp is not None:
                print(f'На текущий момент в городе {temp.city} температура: {temp.reading} {temp.unit}.')
    except KeyboardInterrupt:
        ...
    finally:
        # Выход из группы и фиксация окончательного смещения
        consumer.close()
