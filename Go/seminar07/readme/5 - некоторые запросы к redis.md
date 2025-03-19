# Некоторые запросы к redis

Поднимем standalone valkey с помощью [docker-compose](../code/app05).

## Пакетные запросы (MULTI/EXEC)

Redis позволяет выполнять нечто вроде транзакций. Есть специальная пара запросов `MULTI`/`EXEC`, которая позволяет добавлять команды в очередь, а затем выполнить весь пакет команд целиком.

Запускаем кэш:

```shell
~/backend-academy/backend-academy-2025-spring/Go/seminar07/code/app05 git:[main]
docker-compose up

[+] Running 2/2
 ✔ Network app05_default    Created    0.0s 
 ✔ Container app05-cache-1  Created    0.1s 
Attaching to cache-1
...
cache-1  | 1:M 19 Mar 2025 07:47:13.104 * Server initialized
cache-1  | 1:M 19 Mar 2025 07:47:13.106 * Creating AOF base file appendonly.aof.1.base.rdb on server start
cache-1  | 1:M 19 Mar 2025 07:47:13.108 * Creating AOF incr file appendonly.aof.1.incr.aof on server start
cache-1  | 1:M 19 Mar 2025 07:47:13.108 * Ready to accept connections tcp
```

Подключаемся с помощью redis-cli:

```shell
redis-cli --user admin --pass password
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.
127.0.0.1:6379> ping
PONG
127.0.0.1:6379> keys *
(empty array)
127.0.0.1:6379> get abc
(nil)
127.0.0.1:6379> set abc 123
OK
127.0.0.1:6379> get abc
"123"
127.0.0.1:6379> keys *
1) "abc"
127.0.0.1:6379> MULTI
OK
127.0.0.1:6379(TX)> set def 456
QUEUED
127.0.0.1:6379(TX)> get def
QUEUED
127.0.0.1:6379(TX)> set fgh 789
QUEUED
127.0.0.1:6379(TX)> EXEC
1) OK
2) "456"
3) OK
127.0.0.1:6379> keys *
1) "fgh"
2) "abc"
3) "def"
127.0.0.1:6379> flushdb
OK
```

## PUB/SUB

Есть возможность публиковать события в каналы и подписываться на каналы. Если на канал никто не подписан, то сообщение теряется:

```shell
redis-cli --user admin --pass password
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.
127.0.0.1:6379> publish events ev1
(integer) 0
```

подпишемся из 2 терминалов на канал `events`:

```shell
redis-cli --user admin --pass password
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.
127.0.0.1:6379> subscribe events
1) "subscribe"
2) "events"
3) (integer) 1
```

Снова отправим событие в канал:


```shell
# первый терминал
127.0.0.1:6379> publish events ev2
(integer) 2

# второй и третий терминалы
127.0.0.1:6379> subscribe events
1) "subscribe"
2) "events"
3) (integer) 1
1) "message"
2) "events"
3) "ev2"
Reading messages... (press Ctrl-C to quit or any key to type command)
```

[Примеры кода на golang](https://github.com/redis/go-redis/blob/master/example_test.go#L531).

