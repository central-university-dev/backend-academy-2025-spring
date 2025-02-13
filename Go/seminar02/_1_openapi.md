# OpenAPI
## Основные аспекты

Прикольное подробное описание с примерами

https://starkovden.github.io/about-fourth-module.html

OpenAPI — это спецификация для описания HTTP API, которая позволяет разработчикам и пользователям понять, 
как взаимодействовать с API без необходимости читать его код или документацию. OpenAPI была первоначально 
разработана в рамках проекта Swagger и с тех пор приобрела широкую популярность.

1. OpenAPI использует формат YAML или JSON для описания API, что позволяет автоматически генерировать 
документацию и интерфейсы для тестирования API.
2. OpenAPI позволяет описывать различные типы данных, такие как строки, числа, массивы и объекты, 
а также их ограничения, добавлять описание объектов и методов взаимодействия с АПИ.
3. OpenAPI управляется OpenAPI Initiative, которая является частью Linux Foundation. Сообщество активно развивает 
спецификацию и поддерживает разнообразные инструменты и библиотеки для работы с OpenAPI.

## Примеры использования

Можно создавать интерактивные страницы документации (например, с помощью Swagger UI или Redoc).

- https://swagger.io/tools/swagger-ui/
- https://github.com/Redocly/redoc?tab=readme-ov-file
- https://redocly.github.io/redoc/

Инструменты, такие как Postman, могут импортировать спецификации OpenAPI для облегчения тестирования API.

С помощью Swagger Codegen или OpenAPI Generator разработчики могут создавать шаблоны серверного 
и клиентского кода для различных языков программирования.

## Структура спецификации

https://openapi-map.apihandyman.io/?version=3.0

- `openapi`: версию спецификации OpenAPI, которая используется для описания API (3.0.1 например);
- `info`: информацию о самом API, включает в себя: `title` - заголовок, `description` - описание,
`version` - версия, `contact` - контактные данные;
- `servers`: серверы, на которых развернуто API. Каждый сервер может содержать URL и дополнительные параметры, 
такие как описание и переменные окружения;
- `paths`: маршруты (endpoints) API. Каждая запись пути может содержать различные HTTP методы 
(GET, POST, PUT, DELETE и др.) и описания операций, включая параметры, запросы и ответы;
- `components`: определяет переиспользуемые компоненты. Это помогает избежать дублирования и упростить спецификацию.
  - `schemas` - структуры данных
  - `parameters` - параметры запросов
  - `responses` - возможные ответы
  - `headers` - заголовки
- `security`: указывает механизмы безопасности, которые применяются к API. Это может включать аутентификацию 
через OAuth 2.0, API-ключи, JWT и другие методы;
- `tags`: группирует операции API с использованием тегов для лучшей организации и документирования. Это помогает 
пользователям быстрее находить нужные операции в документации;
- 

## Типы данных

Тип данных формируется из основного типа `type` и необязательного модификатора `format`. Формат дополняет 
описание основного типа и позволяет: отобразить дополнительную информацию в документации и сгенерировать
код с валидацией дополнительного типа данных.

- `string`: для текстовых данных
  - `date-time`
  - `binary`
  - `uuid`
- `number`: для числовых данных (целые числа/с плавающей точкой)
- `integer`: для целочисленных данных
  - `int64`
- `boolean`: для логических значений true или false
- `object`: для структурированных данных, например, объектов JSON; может включать поддерево - описание с простыми типами
- `array`: для упорядоченного списка элементов
- `null`: для отсутствующего значения (я никогда не использовал)

## Подробнее о components

### schemas

Модели данных, используемые в API. Могут быть использованы для определения структуры запросов и ответов. 
Например, модель пользователя может описываться как схемы, содержащие поля, их типы данных и ограничения.

```yaml
components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
        name:
          type: string
        email:
          type: string
          format: email
```

### responses

Стандартные ответы, которые могут быть использованы в разных операциях API. Это может включать как успешные ответы, 
так и сообщения об ошибках, которые могут повторяться для различных маршрутов.

```yaml
components:
  responses:
    NotFound:
      description: Resource not found
    UserResponse:
      description: A single user object
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/User'
```

### parameters

Переиспользуемые параметры запросов, которые могут быть использованы в различных операциях. Это может включать 
как параметры пути (path parameters), так и параметры запроса (query parameters).

```yaml
components:
  parameters:
    UserId:
      name: userId
      in: path
      required: true
      description: ID of the user
      schema:
        type: integer
    UserIds:
      name: userIds
      in: query
      required: false
      description: IDs of the users
      schema:
        type: array
        items:
          type: integer
```

### examples

Примеры данных, которые могут быть использованы для более наглядного описания ответов и запросов. Это может помочь 
разработчикам лучше понять, какие данные ожидаются.

```yaml
components:
  examples:
    UserExample:
      summary: Example User
      value:
        id: 1
        name: "John Doe"
        email: "john.doe@example.com"
```

### requestBodies

Переиспользуемые структуры тела запросов, которые могут быть использованы в различных операциях API. Например, 
если несколько маршрутов требуют одну и ту же структуру данных в теле запроса, можно определить её здесь. Каждый 
объект `requestBody` может содержать описание, требуемую информацию и структуру данных.

```yaml
components:
  requestBodies:
    UserCreation:
      required: true
      description: A User object that needs to be created
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/User'
```

### headers

Помогает стандартизировать использование заголовков в API.

```yaml
components:
  headers:
    X-Rate-Limit:
      description: The number of allowed requests in the current period
      type: integer
    X-Request-Id:
      description: Universal ID used though all request lifetime
      type: string
      format: uuid
```

### securitySchemes

Механизмы безопасности, используемые в API. Это может включать аутентификацию через OAuth 2.0, базовую 
аутентификацию, API-ключи и другие методы. Каждая схема безопасности может быть со ссылкой на соответствующий 
объект в разделе security.

```yaml
components:
  securitySchemes:
    ApiKeyAuth:
      type: apiKey
      in: header
      name: X-API-Key
    OAuth2:
      type: oauth2
      flows:
        authorizationCode:
          authorizationUrl: https://example.com/oauth/authorize
          tokenUrl: https://example.com/oauth/token
          scopes:
            read: Grants read access
            write: Grants write access
```
