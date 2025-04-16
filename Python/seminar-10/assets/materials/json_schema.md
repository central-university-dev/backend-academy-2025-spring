[Вернуться][main]

---

# Создание событий с помощью схемы JSON

## Цели

- Определить схему JSON
- Создать события с помощью Producer, JSONSerializer и Schema Registry

### Python-пакеты jsonschema и requests требуются JSONSerializer, поэтому нам нужно установить их:

```sh
pip install jsonschema
pip install requests
```

### Добавим конфиг для Schema Registry

```py
SR_CONFIG = {
    'url': 'http://schemaregistry:8085',
}
```

### Инициализируем отдельного продюсера для работы с json - [producer_json.py][producer_json]

### Добавим необходимые импорты:

```py
from confluent_kafka import Producer
from confluent_kafka.serialization import SerializationContext, MessageField
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.json_schema import JSONSerializer
```

### Определим класс и схему

```py
class Temperature(object):
    def __init__(self, city, reading, unit, timestamp):
        self.city = city
        self.reading = reading
        self.unit = unit
        self.timestamp = timestamp
```

### Добавим декларацию `schema_str`

```py
schema_str = """{
    "$schema": "https://json-schema.org/draft/2020-12/schema",
    "title": "Temperature",
    "description": "Temperature sensor reading",
    "type": "object",
    "properties": {
      "city": {
        "description": "City name",
        "type": "string"
      },
      "reading": {
        "description": "Current temperature reading",
        "type": "number"
      },
      "unit": {
        "description": "Temperature unit (C/F)",
        "type": "string"
      },
      "timestamp": {
        "description": "Time of reading in ms since epoch",
        "type": "number"
      }
    }
  }"""
```

### Добавим функцию, чтобы преобразовать объект Temperature в словарь

```py
def temp_to_dict(temp, ctx):
    return {
        "city": temp.city,
        "reading": temp.reading,
        "unit": temp.unit,
        "timestamp": temp.timestamp,
    }
```

### Создадим немного тестовых данных

```py
data = [
    Temperature('Rostov', 32, 'C', round(time.time() * 1000)),
    Temperature('Minsk', 24, 'C', round(time.time() * 1000)),
    Temperature('Moscow', 26, 'C', round(time.time() * 1000)),
    Temperature('Sochi', 28, 'C', round(time.time() * 1000)),
    Temperature('Tokio', 15, 'C', round(time.time() * 1000)),
    Temperature('London', 12, 'C', round(time.time() * 1000)),
    Temperature('Chicago', 63, 'F', round(time.time() * 1000)),
    Temperature('Berlin', 14, 'C', round(time.time() * 1000)),
    Temperature('Madrid', 18, 'C', round(time.time() * 1000)),
    Temperature('Phoenix', 78, 'F', round(time.time() * 1000)),
]
```

### Обогатим блок main кодом для отправки данных:

```py
if __name__ == '__main__':
    # Создается экземпляр SchemaRegistryClient
    schema_registry_client = SchemaRegistryClient(SR_CONFIG)
    # Создается экземпляр JSONSerializer
    json_serializer = JSONSerializer(schema_str,
                                     schema_registry_client,
                                     temp_to_dict)
    # Создается экземпляр Producer
    producer = Producer(KAFKA_CONFIG)

    # Создание данные
    for temp in data:
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
```

- Создаём экземпляр `SchemaRegistryClient`, используя `SR_CONFIG`. 
- Затем создаём экземпляр `JSONSerializer` с классом `Temperature`, `SchemaRegistryClient` и функцией `temp_to_dict`. 
- Затем создаём экземпляр `Producer` с конфигурацией, которую мы использовали в примерах.
- Далее будем выполнять итерации над нашими тестовыми данными и создавать события.
  > Обратите внимание, что ключ события - это строка, а значение события сериализуется с помощью `JSONSerializer`. 
- Наконец, мы вызываем producer.flush(), чтобы убедиться, что все события отправлены и обратные вызовы выполнены.

### Откроем UI http://localhost:9021/clusters и посмотрим на процесс создания

---

[Вернуться][main]


[main]: ../../README.md "содержание"

[producer_json]: ./src/producer_json.py "producer_json"