### Сборка приложения с помощью sbt-native-packager

Рассмотрим сборку в docker образ нашего приложения через sbt-native-packager.
Docs https://sbt-native-packager.readthedocs.io/en/stable/

Команды для сборки и работы с образом:

```bash
# Cборка образа
sbt "seminar-4 / docker:publishLocal"

# Проверка что образ появился в локальном registry
docker images

# Посмотрим что внутри образа; Используется версия 0.1.0-SNAPSHOT т.к. она указана в build.sbt
docker inspect tbank-ab:0.1.0-SNAPSHOT

# Запустить compose (выполняется в папке с docker-compose.yaml)
docker compose up

# Посмотреть запущенные контейнеры 
docker container ls
```

### Интеграционные тесты

Есть несколько вариантов запуска тестов, рассмотрим парочку:

- Поднятие окружения:
    - Собираем образ нашего приложения
    - Публикуем в свой registry (можно не публиковать, тут суть в том, чтобы передать собранный образ на следующий шаг)
    - Поднимаем полностью всё окружение с помощью docker-compose (или через другой инструмент)
    - Выполняем интеграционные тесты (причём тут уже не важно на каком языке написаны тесты); скорее всего в виде http
      запросов к нашему контейнеру
- Поднимаем наше приложение без сборки образа:
    - Поднимаем зависимости через testcontainers
    - Перед началом тестов поднимаем наш Main class в отдельном потоке
    - Прогоняем scala тесты

### Рассмотрим оба варианта

Давайте добавим в наш проект endpoint который будет обращаться к внешнему источнику и возвращать ответ.

#### docker compose app + mockserver

Добавьте в docker compose mockserver

#### scala test + testcontainers mockserver

В тесты уже добавлен mockserver, запустить тесты можно командой `sbt "seminar-4-it / Test / test"`. Примечание, в вашем
окружении должен быть `docker` (т.к. будет запущен контейнер с mockserver)

Т.к. наше приложение ещё никак не зависит на внешний источник (наше приложение нигде не вызывает внешнюю интеграцию, и
всё хранит в памяти), то мы просто поднимем mockserver и покажем работу с ним на примере создания endpoint аналогичного
`live` probe и вызовем его из тестов.

В вашем домашнем задании придётся слегка усложнить данную логику - если вы хотите использовать mockserver, для подмены
внешних интеграций для выполнения чистых тестов, то вам придётся научиться передавать host:port моксервера в конфиг
(подсказка: есть много вариантов как это можно сдлеать, например явно передавать `AppConfig` для инициализации вашего
приложения; или вы можете переопределить конфиг используя не `ConfigSource.default`, а предоставленный (в `ConfigSource`
можно задать свои значения поверх дефолтных))
