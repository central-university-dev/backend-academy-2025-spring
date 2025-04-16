[Вернуться][main]

---

# Создание Продюсера

В системе Apache Kafka участвуют три основных компонента:
 - производители (Producers)
 - брокеры (Brokers)
 - потребители (Consumers)

**Производители (Producers)**

Производители отвечают за генерацию потока данных и запись этого потока в один или несколько топиков Kafka.
Производители отправляют данные в брокеры, не заботясь о том, к какому брокеру и партиции они идут. Kafka сама
обеспечивает распределение данных по партициям в каждом топике.


## Пример продюсера

```py
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


data = {'key': 'value'}
p.produce('test_topic', json.dumps(data).encode('utf-8'), callback=delivery_report)
p.poll(0)

# Ожидание отправки всех сообщений  
p.flush()  
```

---

[Вернуться][main]


[main]: ../../README.md "содержание"

[python kafka-client]: https://docs.confluent.io/kafka-clients/python/current/overview.html "python kafka-client"