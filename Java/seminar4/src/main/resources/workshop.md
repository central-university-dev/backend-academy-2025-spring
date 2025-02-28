# Основные принципы

- Что такое виртуализация, эмуляция, контейнеризация
    - **Виртуализация** – процесс создания и обеспечения работоспособности полностью виртуальной среды, включая как
      аппаратную составляющую (процессор, сетевое оборудование, дисковая подсистема) так и программную (операционная
      система). Используется гипервизор 1-го (запускается на оборудовании = базовая ОС) или 2-го (запускается поверх
      хостовой ОС) типов
    - **Эмуляция** – процесс имитации аппаратной или программной среды, чаще всего посредством трансляции инструкций в
      режиме реального времени. Эмуляторы могут также использовать специализированное оборудование для ускорения
    - **Контейнеризация** – запуск средствами ядра хостовой ОС, инкапсулируют не только одно приложение, но и всё
      сопутствующее окружение, включая библиотеки. Идеально подходит для оперативного развертывания готовых приложений
      или его отдельных компонентов
    - **Производительность**: эмуляция < виртуализация < контейнеризация

# Установка Docker

- https://docs.docker.com/engine/install/

# Образы, контейнеры и реестры Docker

- **Образ**
    - Базовая единица собранного в кучу ПО, содержит все необходимое окружение
    - Аналогия из ООП – класс
    - Собирается с помощью Dockerfile
    - Собирается из различных неиммутабельных и переиспользуемых слоёв (об этом – позднее)
- **Контейнер**
    - Создается из образа, может быть запущен, причем каждый контейнер изолирован как от хостовой ОС, так и от других
      контейнеров
    - Аналогия из ООП – экземпляр класса
    - Контейнер = образ + набор аргументов
- **Реестр образов**
    - Бинарный репозиторий образов
    - Аналог git или maven-central, только для контейнеров
    - Docker по умолчанию использует [Docker Hub](https://hub.docker.com/)

# Основные команды Docker

Загружаем образ:

```sh
docker pull hello-world
```

Запускаем образ (автоматически создается контейнер):

```sh
docker run hello-world
docker run -d hello-world # -d = detach, контейнер будет работать в фоне
```

Запускаем завершивший работу контейнер:

```sh
docker start [id]
docker start -a [id] # прицепляемся к STDOUT/STDERR + перенаправляем сигналы в контейнер из терминала
```

Смотрим, что у нас с контейнерами:

```sh
docker ps # выводит только активные
docker ps -a # выводит все
```

Удаляем:

```sh
docker rm [id]
```

Еще примеры:

```sh
docker run --name webserver nginx

docker run --name webserver -d nginx

docker run --name webserver --rm -d nginx

docker run --name webserver -d -p 8080:80 nginx
docker logs -f webserver
docker stop webserver

docker run --name webserver 

docker run --name nginx-interactive -it --rm nginx /bin/bash # -i прицепиться к stdin, -t аллоцировать терминал внутри контейнера, интерфейс для взаимодействия

docker run --name webserver --rm -p 8080:80 nginx
docker exec -it webserver /bin/bash
```

Используем network:

```sh
docker network create seminar

docker run --name webserver --rm -d --network seminar nginx
docker run --rm -it --network seminar alpine

docker run --rm --network host nginx # работает только на Linux
```

Docker использует [несколько](https://docs.docker.com/network/drivers/) режимов работы с сетью. Наиболее распространены
следующие:

- [Bridge](https://docs.docker.com/network/drivers/bridge/) – создается промежуточный виртуальный сетевой интерфейс
  между хостом и контейнером. Предоставляет изоляцию между контейнерами и хостом. Работает **по умолчанию**, под капотом
  используется Iptables
- [Host](https://docs.docker.com/network/drivers/host/) – отсутствует изоляция сети между контейнерами и хостом,
  контейнер начинает работать напрямую с сетевым интерфейсом хоста. На Mac и Windows может не работать, так как Docker
  на этих платформах запускается в виртуальной машине!

Используем volume:

```sh
# создаем volume, который управляется докером, и монтируем его в контейнер
docker volume create postgres-data
docker run --name postgres -e POSTGRES_PASSWORD=password -d -v postgres-data:/var/lib/postgresql/data postgres
docker exec -it postgres /bin/bash

# монтируем локальную директорию в контейнер
mkdir -p $HOME/docker/volumes/postgres 
docker run --name postgres -e POSTGRES_PASSWORD=password -d -v $HOME/docker/volumes/postgres:/var/lib/postgresql/data postgres
```

# Dockerfile

Специальный файл, в котором указываются [директивы](https://docs.docker.com/engine/reference/builder/) для
самостоятельной сборки образов.

Директория, в которой будет сборка:

```sh
mkdir nginx-image && cd nginx-image
```

Dockerfile:

```Dockerfile
FROM nginx:latest 
 
COPY index.html /usr/share/nginx/html/index.html 
 
EXPOSE 80
```

Кастомная страница приветствия:

```html
<!DOCTYPE html>
<html>
<head>
    <title>Welcome to My Nginx Container!</title>
    <style>
        body {
            width: 35em;
            margin: 0 auto;
            font-family: Tahoma, Verdana, Arial, sans-serif;
        }
    </style>
</head>
<body>
<h1>Welcome to My Nginx Container!</h1>
<p>If you see this page, the nginx web server is successfully installed and working.</p>
</body>
</html>
```

Cобираем и запускаем образ:

```sh
docker build -t nginx-custom .

docker run --rm -p 8080:80 nginx-custom
```

- Каждый образ состоит из [слоев](https://docs.docker.com/build/guide/layers/)
- Каждая директива в Dockerfile = создание нового слоя образа поверх предыдущего слоя.
- Каждый слой неиммутабелен и кэшируется докером для ускорения процесса сборки
- При изменении какой-либо директивы в Dockerfile будут переипользованы слои, которые были созданы выше этой директивы.
  Все слои, что находятся ниже директивы, будут созданы заново

# docker-compose

[Docker Compose](https://docs.docker.com/compose/) – инструмент для развертывания системы, состоящей из множества
компонентов. Позволяет развернуть одной командой целый стек из приложений, разнесенных по сетям, использующие разные
volume'ы и т.д.
Существуют две версии:

- **Compose V1** – python-cкрипт, запускается командой `docker-compose`
- **Compose V2** – поставляется как часть Docker Desktop, является расширением для Docker, запускается командой
  `docker compose` (по умолчанию Docker Desktop настраивает alias, чтобы при выполнении `docker-compose` запускался *
  *Compose V2**)

Собираем образ [spring-petclinic](https://github.com/spring-projects/spring-petclinic) из исходников целиком внутри
контейнера:

```Dockerfile
FROM eclipse-temurin:21-alpine AS builder

RUN apk add --no-cache git
RUN git clone https://github.com/spring-projects/spring-petclinic.git

WORKDIR spring-petclinic
RUN git reset --hard 6328d2c
ENV MAVEN_ARGS="-B -Dmaven.artifact.threads=10 -Dmaven.test.skip=true -Dmaven.source.skip -Dmaven.javadoc.skip=true"
RUN --mount=type=cache,target=/root/.m2 ./mvnw package
RUN mv target/*.jar app.jar
  
FROM eclipse-temurin:21-alpine

COPY --from=builder spring-petclinic/app.jar ./

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Если решили собрать локально, то используем `layertools` для оптимизации скорости сборки образа (распаковываем `jar` и
разносим его составляющие по различным слоям образа, чтобы при изменении в исходном коде пересобирался только последний
слой):

```Dockerfile
FROM eclipse-temurin:21-alpine AS builder

WORKDIR spring-petclinic
СOPY target/*.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract
  
FROM eclipse-temurin:21-alpine
  
COPY --from=builder spring-petclinic/dependencies/ ./
COPY --from=builder spring-petclinic/snapshot-dependencies/ ./
COPY --from=builder spring-petclinic/spring-boot-loader/ ./
COPY --from=builder spring-petclinic/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

Используем собранный образ вместе с базой:

```yaml
services:

    postgres:
        image: postgres:16-alpine
        environment:
            - POSTGRES_USER=petclinic
            - POSTGRES_PASSWORD=petclinic
        volumes:
            - ./postgres-data:/var/lib/postgresql/data
        healthcheck:
            test: [ "CMD", "pg_isready", "-U", "petclinic", "-d", "petclinic" ]
            interval: 3s
            timeout: 10s
            retries: 5
            start_period: 20s

    spring-petclinic:
        build: .
        depends_on:
            postgres:
                condition: 'service_healthy'
        environment:
            - SPRING_PROFILES_ACTIVE=postgres
            - POSTGRES_URL=jdbc:postgresql://postgres/petclinic
        ports:
            - "8080:8080"
```
