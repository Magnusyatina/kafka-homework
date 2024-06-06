# Домашнее задание №5. Разработка приложения Kafka Streams

## Описание решаемой задачи.

Требуется разработать приложение, которое подсчитывает количество событий с одинаковым key в рамках сессии 5 минут

Требования:
1. Создать топик events;
2. Публиковать сообщения в топик через console producer;
3. Для анализа данных использовать Kafka Streams API.

## Описание тестового контура.

- 3 брокера кафка;
- Фактор репликации 3;
- Количество партиций на топик 3.

Конфигурация тестового контура представлена в файле [docker-compose.yml](docker-compose.yml)

## Ход выполнения поставленной задачи

1. Создадим топик events при помощи следующей команды:

```shell
kafka-topics --bootstrap-server kafka1:9092 --topic events --create
```

![topic-create-result.png](images%2Ftopic-create-result.png)

2. Отправим несколько сообщений с ключами:

![produced-messages.png](images%2Fproduced-messages.png)

3. Запустим анализирующий поток:

![streaming-result.png](images%2Fstreaming-result.png)

Топология потока:
![topology.png](images%2Ftopology.png)

При работе потока были созданы дополнительные топики:

Поток имеет два sink метода:
- Вывод результатов в консоль;
- Публикация результатов в отдельный топик events-count-by-key. 

![broker-topics.png](images%2Fbroker-topics.png)

- event-count-app-event-counts-store-changelog - топик с промежуточными результатами;
- event-count-app-event-counts-store-repartition - топик, созданный из-за map конструкции (необходимо было для отладки кода);
- events-count-by-key - топик с результатами.


Код программы представлен в файле [Application.java](src%2Fmain%2Fjava%2Fcom%2Fmagnusario%2FApplication.java)