#!/usr/bin/env python

from string import ascii_uppercase, digits
from random import choice
from confluent_kafka import Producer

from kafka_utils import KAFKA_CONFIG, DEFAULT_TOPIC, delivery_callback


def id_generator(size: int = 6, chars=ascii_uppercase + digits):
    return ''.join(choice(chars) for _ in range(size))


if __name__ == '__main__':
    # Создается экземпляр Producer
    producer = Producer(KAFKA_CONFIG)

    # Создание данных путём выбора случайных значений из этих списков.
    user_ids = [id_generator() for _ in range(6)]
    products = ['credit card', 'mobile', 'mortgage', 'debit card', 'insurance']

    for _ in range(100):
        user_id = choice(user_ids)
        product = choice(products)
        producer.produce(DEFAULT_TOPIC, product, user_id, callback=delivery_callback)

    # Блокировка до отправки сообщений.
    producer.poll(10000)
    producer.flush()
