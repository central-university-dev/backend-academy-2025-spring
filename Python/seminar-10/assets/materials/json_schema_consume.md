[Вернуться][main]

---

# Потребление событий с помощью схемы JSON

## Цели

- использовать созданные события и с помощью JSONDeserializer превратить их в объекты, с которыми можно
  работать в приложениях на Python.

### Инициализируем отдельного консюмера для работы с json - [consumer_json.py][consumer_json]

### Добавим необходимые импорты:

```py
from confluent_kafka.serialization import SerializationContext, MessageField
from confluent_kafka.schema_registry.json_schema import JSONDeserializer
from confluent_kafka import Consumer, OFFSET_BEGINNING

from assets.materials.src.temp_data import Temperature
from kafka_utils import CONSUMER_CONFIG, TEMP_TOPIC
```

### Добавим функцию для преобразования словаря в объект Temperature:

```py
def dict_to_temp(dict_: dict, ctx):
    return Temperature(
        dict_['city'],
        dict_['reading'],
        dict_['unit'],
        dict_['timestamp'],
    )
```

### Обогатим блок main кодом для получения данных:

```py
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
            temp = json_deserializer(event.value(),
                                     SerializationContext(TEMP_TOPIC, MessageField.VALUE))
            if temp is not None:
                print(f'На текущий момент в городе {temp.city} температура: {temp.reading} {temp.unit}.')
    except KeyboardInterrupt:
        ...
    finally:
        # Выход из группы и фиксация окончательного смещения
        consumer.close()
```

- Сначала создаём экземпляр JSONDeserializer на основе схемы температур.
- Создаём экземпляр потребителя.
- Подписываемся на топик.
- Добавляем цикл опроса для получения и обработки событий.

> В цикле опроса будем использовать экземпляр `JSONDeserializer`, чтобы превратить двоичные данные о событиях в
> пригодные для использования объекты данных.

---

[Вернуться][main]


[main]: ../../README.md "содержание"

[consumer_json]: ./src/consumer_json.py "consumer_json"