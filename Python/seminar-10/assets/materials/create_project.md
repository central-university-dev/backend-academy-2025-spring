[Вернуться][main]

---

# Создание проекта

## Окружение

Создай и активируй виртуальное окружение:

```sh
virtualenv env
source env/bin/activate
```

Установи библиотеку [клиента][python kafka-client] Apache Kafka для Python:

```
pip install confluent-kafka
```

## Контейнеры

Описываем логику в компоуз-файле:

[docker-compose.yml](../../docker-compose.yml)

Вспомогательные файлы:

- [.env](../../.env)
- [init-kafka-broker.sh](../../docker/kafka/init-kafka-broker.sh)
- [kafka_server_jaas.conf](../../docker/kafka/kafka_server_jaas.conf)
- [docker-compose.kafka.yml](../../docker/kafka/docker-compose.kafka.yml)
- [docker-compose.zookeeper.yml](../../docker/zookeeper/docker-compose.zookeeper.yml)
- [docker-compose.schemaregistry.yml](../../docker/schemaregistry/docker-compose.schemaregistry.yml)
- [docker-compose.control-center.yml](../../docker/control-center/docker-compose.control-center.yml)

Запускаем контейнеры:

```bash
docker-compose up -d
```

Документация Confluent по взаимодействию со
schema-registry - [Schema Registry API Usage Examples](https://docs.confluent.io/platform/current/schema-registry/develop/using.html)


---

[Вернуться][main]


[main]: ../../README.md "содержание"

[python kafka-client]: https://docs.confluent.io/kafka-clients/python/current/overview.html "python kafka-client"