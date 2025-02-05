# fasthttp

Пакет `github.com/valyala/fasthttp` является очень популярной библиотекой, реализующей http-сервер и обработчики. Обработчики имеют немного другую сигнатуру, а сервер создаётся и запускается немного по-другому. Маршрутизация обработчиков, аналогичная мультиплексору из стандартной библиотеки, в самом пакете не поддержана, но есть ряд дополнительных пакетов, в которых это реализовано, например, `github.com/fasthttp/router`. 

Код с использованием `fx` и `net/http` достаточно просто адаптировать под `fasthttp`. [Пример кода](../code/part3/fasthttp).

Следует также иметь в виду [комментарии разработчиков](https://github.com/valyala/fasthttp?tab=readme-ov-file#fasthttp-might-not-be-for-you) библиотеки:

> fasthttp was designed for some high performance edge cases. Unless your server/client needs to handle thousands of small to medium requests per second and needs a consistent low millisecond response time fasthttp might not be for you. For most cases net/http is much better as it's easier to use and can handle more cases. For most cases you won't even notice the performance difference.
