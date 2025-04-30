# Retry

В распределённой инфраструктуре запрос к внешней системе может не выполниться успешно, по разным причинам. Запрос в таких случаях обычно повторяют.

Для того, чтобы запрос можно было безопасно ретраить, он должен быть идемпотентным. [Статья про идемпотентные запросы на хабре](https://habr.com/ru/companies/yandex/articles/442762/).

Нужно учитывать влияние повторяющихся запросов на запрашиваемую систему. Возможно, сбой запрос произошёл по причине деградации внешней системы, и повторяющиеся попытки запроса только ухудшат ситуацию. Больше о том, как правильно ретраить, можно почитать в [статье про ретраи на хабре](https://habr.com/ru/companies/yandex/articles/762678/).

Для использования в коде на golang подходит пакет `github.com/avast/retry-go/v4`. Он поддерживает разные опции: количество ретраев, различную обработку ошибок, фиксированную и случайную (jitter) задержки между ретраями, и пр. Пример, как выглядит использование:

```go
  config, err = retry.DoWithData(
      func() (partitionConfig, error) {
          resp := (&request.Client{
              URL:     fmt.Sprintf("http://%s/config", c.patroniURL),
              Method:  request.GET,
              Timeout: timeout,
          }).Send()
          defer resp.Close()
  
          var body partitionConfig
          if err = resp.ScanJSON(&body).Error(); err != nil {
              return partitionConfig{}, errTypes.PatroniError.Wrap(err, "couldn't load patroni config")
          }
  
          return body, nil
      },
      retry.Attempts(retries),
      retry.Delay(delay),
      retry.LastErrorOnly(true),
      retry.Context(ctx),
  )
  
  if err != nil {
      return nil, err
  }
  
  return &config.Postgres.SSLConfig, nil
```
