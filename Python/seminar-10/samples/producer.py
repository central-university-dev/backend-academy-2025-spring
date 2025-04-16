import json

from confluent_kafka import Producer

# Настройки для Producer
p = Producer({'bootstrap.servers': 'localhost:9092'})


# Функция обратного вызова для подтверждения доставки сообщения
def delivery_report(err, msg):
    if err is not None:
        print('Message delivery failed: {}'.format(err))
    else:
        print('Message delivered to {} [{}]'.format(msg.topic(), msg.partition()))

    # Отправка сообщения


if __name__ == '__main__':
    data = {'key': 'value'}
    p.produce('test_topic', json.dumps(data).encode('utf-8'), callback=delivery_report)
    p.poll(0)

    # Ожидание отправки всех сообщений
    p.flush()
