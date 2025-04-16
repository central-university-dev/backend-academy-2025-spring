#!/usr/bin/env python

from confluent_kafka import Consumer, OFFSET_BEGINNING

from kafka_utils import CONSUMER_CONFIG, DEFAULT_TOPIC


def reset_offset(consumer_: Consumer, partitions, reset: bool = False):
    if reset:
        for p in partitions:
            p.offset = OFFSET_BEGINNING
        consumer_.assign(partitions)


if __name__ == '__main__':
    # Создается экземпляр Consumer
    consumer = Consumer(CONSUMER_CONFIG)

    # Подписка на тему
    consumer.subscribe([DEFAULT_TOPIC], on_assign=reset_offset)

    # Опрос новых сообщений от Кафки и вывод полученного результата в терминал.
    try:
        while True:
            msg = consumer.poll(1.0)
            if msg is None:
                # Первоначальное потребление сообщения может занять до `session.timeout.ms`,
                # чтобы группа потребителей перебалансировалась и начала потребление
                print('Waiting...')
            elif msg.error():
                print(f'ERROR: {msg.error()}')
            else:
                # Извлечение (необязательного) ключа и значения и вывод в терминал.
                key = msg.key().decode('utf-8')
                value = msg.value().decode('utf-8')
                print(f'Consumed event from topic `{msg.topic()}`: key = {key:12} value = {value:12}')
    except KeyboardInterrupt:
        pass
    finally:
        # Выход из группы и фиксация окончательного смещения
        consumer.close()
