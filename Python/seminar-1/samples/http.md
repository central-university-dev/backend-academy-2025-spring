#### Пример HTTP GET запроса:

* HTTP/1.0

```http request
GET /index.html HTTP/1.0
Host: www.example.com
User-Agent: Mozilla/5.0
Accept: text/html
```


* HTTP/1.1
```http request
GET /index.html HTTP/1.1
Host: www.example.com
User-Agent: Mozilla/5.0
Accept: text/html
Connection: keep-alive
```


* HTTP/2.0
```http request
:method: GET
:scheme: https
:authority: www.example.com
:path: /index.html
User-Agent: Mozilla/5.0
Accept: text/html
```


* HTTP/3.0

HTTP/3 работает на основе QUIC и использует схему, похожую на HTTP/2, но с дополнительными улучшениями. Прямое представление HTTP/3 в текстовом виде не так просто, как в HTTP/1.1 и HTTP/2, но концептуально оно выглядит очень похоже на HTTP/2.

QUIC (Quick UDP Internet Connections) — это протокол транспортного уровня, разработанный Google, который предназначен для повышения производительности и безопасности веб-приложений. Он был изначально разработан для улучшения работы HTTP/2 и обеспечивания более высокой скорости передачи данных по сравнению с классическим TCP, который является стандартным транспортным протоколом для большинства интернет-трафика.

Пример HTTP/3 не будет значимо отличаться в своей логике от HTTP/2, и можно рассматривать HTTP/3 как обеспечивающий такую же семантику, но использующий другие механизмы доставки данных.

```http request
:method: GET
:scheme: https
:authority: www.example.com
:path: /index.html
User-Agent: Mozilla/5.0
Accept: text/html
```