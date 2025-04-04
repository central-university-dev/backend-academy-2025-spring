# Работа с Kafka

## Kafka и Zookeeper

Apache Kafka - это распределённый брокер сообщений, исторически работающий на основе координации Zookeeper, хотя последние версии Kafka, реализующе KRaft, позволяют работать самостоятельно.

## Основные понятия

В Kafka единицей представляения данных являются сообщения (логи).

Сообщения в Kafka не удаляются брокерами по мере их обработки консьюмерами — данные в Kafka могут храниться днями, неделями, годами.

Благодаря этому одно и то же сообщение может быть обработано сколько угодно раз разными консьюмерами и в разных контекстах.

Семантика доставки сообщений (из коробки) - At most once если репликация асинхронная и At least once если синхронная.

Типы репликаций:

1. **Синхронная.** Вновь пришедшие данные реплицируются синхронно на несколько нод.
2. **Асинхронная.** Вновь пришедшие данные считаются записанными в систему тогда когда они записаны на 1 или несколько нод. Репликация на остальные ноды происходит уже после этого. Для каждой партиции лидером могут являться различные ноды.

Каждая партиция в Kafka имеет одну основную реплику, которая называется лидером. Лидер отвечает за прием и запись всех новых сообщений в партиции. Он служит как точка доступа для клиентов, отправляющих и получающих данные. Лидер гарантирует упорядоченность сообщений в партиции и контролирует запись данных.

Каждая партиция также имеет реплики (фолловеры). Реплики являются копиями данных из лидера. Они хранят данные в синхронизированном состоянии с лидером. Если лидер становится недоступным, одна из реплик может быстро перейти в роль лидера.

Лидер регулярно отправляет данные фолловерам, чтобы они могли обновлять свои копии данных и оставаться в синхронизированном состоянии.

Клиенты, отправляющие запросы на запись данных, обращаются к лидеру.

## Разворачиваем Kafka

Добавим в docker-compose 3 сервиса: zookeeper, kafka и kafka-setup, последний создаст на сервере Kafka нужные нам топики:

```yam
zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_SERVER_ID: 1
      ZOOKEEPER_TICK_TIME: 2000
      ZOOKEEPER_INIT_LIMIT: 5
      ZOOKEEPER_SYNC_LIMIT: 2
      ZOOKEEPER_SASL_ENABLED: "false"
    expose:
      - 2181
    healthcheck:
      test: "nc -z localhost 2181 || exit -1"
      start_period: 10s
      interval: 5s
      timeout: 10s
      retries: 10

  kafka:
    image: confluentinc/cp-kafka:latest
    expose:
      - 9093
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: "zookeeper:2181"
      KAFKA_ADVERTISED_LISTENERS: "BROKER://kafka:9093,EXTERNAL://localhost:9092"
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: "BROKER:PLAINTEXT,EXTERNAL:PLAINTEXT"
      KAFKA_INTER_BROKER_LISTENER_NAME: "BROKER"
      KAFKA_BROKER_ID: "1"
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: "1"
      KAFKA_CONFLUENT_SUPPORT_METRICS_ENABLE: "false"
      ZOOKEEPER_SASL_ENABLED: "false"
    depends_on:
      zookeeper:
        condition: service_healthy
    healthcheck:
      test: "nc -z localhost 9093 || exit -1"
      start_period: 10s
      interval: 5s
      timeout: 10s
      retries: 10

  kafka-setup:
    image: confluentinc/cp-kafka:latest
    depends_on:
      kafka:
        condition: service_healthy
    environment:
      KAFKA_BROKER_ID: ignored
      KAFKA_ZOOKEEPER_CONNECT: ignored
    command: "bash -c 'echo Waiting for Kafka to be ready... && \
              cub kafka-ready -b kafka:9093 1 30 && \
              echo Creating topics... && \
              kafka-topics --create --bootstrap-server kafka:9093 --partitions 1 --replication-factor 1 --topic animals-input && \
              kafka-topics --create --bootstrap-server kafka:9093 --partitions 1 --replication-factor 1 --topic animals-update'"
```

## Kafka CLI

Инструмент взаимодействия с Kafka из терминала. Мы уже использовали его, когда создавали топики в kafka-setup:

```bash
kafka-topics --create --bootstrap-server kafka:9093 --partitions 1 --replication-factor 1 --topic animals-input
```

### Также среди полезных команд можно отметить:

Считаем, что все перечисленные ниже команды запсукаются из контейнера с kafka сервером:

Перечисление существующих топиков:

```bash
kafka-topics --list --bootstrap-server kafka:9093
```

Вывод информации по топику:

```bash
kafka-topics --bootstrap-server kafka:9093 --describe --topic animals-input

```

Продюсинг сообщения в топик:

```bash
kafka-console-producer --broker-list localhost:9092 --topic animals-input --property "parse.key=true" --property "key.separator=||"

```

Далее передаются параметры, например, `rabbit||{"description":"description","habitat":"forest","features":[]}`

## Подключние к Kafka. Producer

Основные параметры конфигурации Producer:

* bootstrap.servers
* retries
* batch.size
* client.id
* delivery.timeout.ms
* linger.ms
* security.protocol
* sasl.mechanism
* sasl.jaas.config
* acks [-1, 0, 1]

## Подключение к Kafka. Consumer

Основные параметры конфигурации Consumer:

* bootstrap.servers
* fetch.min.bytes и fetch.max.bytes
* group.id
* max.partition.fetch.bytes
* auto.offset.reset [latest, earliest, none]
* enable.auto.commit
* max.poll.interval.ms
* security.protocol
* sasl.mechanism
* sasl.jaas.config
