[Вернуться][main]

---

# Потребление событий

## Из контейнера

запускаем продюсер:

```bash
kafka-console-consumer --bootstrap-server broker1:29092 --topic test-topic --from-beginning
```

## Извне с помощью кода на Python:

Запустим [консюмер][consumer] для чтени я из топика `tink_backend_academy`

В консоли увидим:

```sh
Consumed event from topic `tink_backend_academy`: key = htanaka      value = t-shirts    
Consumed event from topic `tink_backend_academy`: key = htanaka      value = insurance   
Consumed event from topic `tink_backend_academy`: key = htanaka      value = t-shirts    
Consumed event from topic `tink_backend_academy`: key = awalther     value = credit card 
Consumed event from topic `tink_backend_academy`: key = htanaka      value = debit card  
Consumed event from topic `tink_backend_academy`: key = awalther     value = mobile      
Consumed event from topic `tink_backend_academy`: key = awalther     value = credit card 
Consumed event from topic `tink_backend_academy`: key = htanaka      value = insurance   
Consumed event from topic `tink_backend_academy`: key = BI72WH       value = credit card 
Consumed event from topic `tink_backend_academy`: key = 9FSU8I       value = t-shirts    
Consumed event from topic `tink_backend_academy`: key = 2FAWSD       value = credit card 
Consumed event from topic `tink_backend_academy`: key = 9FSU8I       value = debit card  
Consumed event from topic `tink_backend_academy`: key = BI72WH       value = mobile      
Consumed event from topic `tink_backend_academy`: key = 9FSU8I       value = credit card 
Consumed event from topic `tink_backend_academy`: key = 9WKASC       value = credit card 
Consumed event from topic `tink_backend_academy`: key = 300DLM       value = mobile      
Consumed event from topic `tink_backend_academy`: key = 300DLM       value = mortgage    
Consumed event from topic `tink_backend_academy`: key = SUH156       value = mobile      
Consumed event from topic `tink_backend_academy`: key = 087N9A       value = mobile      
Waiting...
Waiting...
Waiting...
```

---

[Вернуться][main]


[main]: ../../README.md "содержание"

[consumer]: ./src/consumer.py "consumer.py"

[python kafka-client]: https://docs.confluent.io/kafka-clients/python/current/overview.html "python kafka-client"