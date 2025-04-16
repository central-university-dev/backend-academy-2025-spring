from confluent_kafka import Consumer, KafkaError

if __name__ == '__main__':

    # Настройки для Consumer
    c = Consumer({
        'bootstrap.servers': 'localhost:9092',
        'group.id': 'mygroup',
        'auto.offset.reset': 'earliest'
    })

    c.subscribe(['test_topic'])

    # Потребление сообщений
    while True:
        msg = c.poll(1.0)

        if msg is None:
            continue
        if msg.error():
            if msg.error().code() == KafkaError._PARTITION_EOF:
                # Достигнут конец партиции
                continue
            else:
                print(msg.error())
                break

        print('Received message: {}'.format(msg.value().decode('utf-8')))

    c.close()