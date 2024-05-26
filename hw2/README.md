# Домашнее задание №2. Разворачивание Kafka с Kraft и настройка безопасности

## 1. Подготовка к первому запуску kafka брокера с kraft

Сгенерируем идентификатор кластера при помощи следующей команды:

```shell
bin/kafka-storage.sh random-uuid
```

Создадим директорию kraft-combined-logs при помощи mkdir и отформатируем ее следующей командой 
с передачей сгенерированного идентификатора кластера и указанием конфигурации сервера 
(за основу конфигурационного файла брокера возьмем исходный файл из директории 
config/kraft/server.properties):

```shell
bin/kafka-storage.sh format -t $KAFKA_CLUSTER_ID -c config/kraft/server.properties
```

Результат выполненных операций:
![preparing_the_server_for_launch.png](images%2Fpreparing_the_server_for_launch.png)

После совершенных операций брокер успешно стартует:
![first_broker_launch.png](images%2Ffirst_broker_launch.png)

## 2. Настройка SASL/PLAIN для kafka

Для настройки SASL/PLAIN необходимо прописать соответствующие настройки в конфигурации брокера:

```properties
listeners=SASL_PLAINTEXT://:9092,CONTROLLER://:9093
advertised.listeners=SASL_PLAINTEXT://localhost:9092
controller.listener.names=CONTROLLER
listener.security.protocol.map=CONTROLLER:SASL_PLAINTEXT,PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL

sasl.enabled.mechanisms=PLAIN # включаем механизм аутентификации SASL/PLAIN
sasl.mechanism.controller.protocol=PLAIN # включаем механизм аутентификации для взаимодействия с контроллером SASL/PLAIN
sasl.mechanism.inter.broker.protocol=PLAIN # включаем механизм аутентификации между брокерами SASL/PLAIN
security.inter.broker.protocol=SASL_PLAINTEXT # указываем протокол безопасности для взаимодействия брокеров между собой

# Для брокера определяем список имя пользователя=пароль для аутентификации
listener.name.broker.plain.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
    username="kafka" \
    password="kafka-password" \
    user_kafka="kafka-password";

# Для контроллера определяем список имя пользователя=пароль для аутентификации
listener.name.controller.plain.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
    username="kafka" \
    password="kafka-password" \
	user_kafka="kafka-password";

# Задаем перечень пользователей для аутентификации в kafka
listener.name.sasl_plaintext.plain.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
	username="kafka" \
	password="kafka-password" \
	user_kafka="kafka-password" \
	user_Alice="Alice-password" \
	user_Bob="Bob-password" \
	user_TOM="Tom-password";
```

Основным пользователем для брокера является Kafka с паролем kafka-password. Так же были явно заданы 3 других пользователя,
которым будут выданы соответствующие права:
- Alice
- Bob
- Tom

Для каждого из пользователей создадим соответствующие properties файлы для аутентификации со следующим содержанием:
```properties
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule \
	required username="Alice" password="Alice-password";
```

Соответственно у каждого пользователя должны бы соответствующие им username и password

Для проверки работоспособности текущего конфигурационного файла выполним следующие шаги:
1. Запустим брокер с новым конфигурационным файлом:

```shell
bin/kafka-server-start.sh config/kraft/sasl-server.properties
```

2. Попробуем прослушать топик без задания конфигурации пользователя следующей командой:

```shell
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --group test3 --from-beginning
```

В результате в логах появляются следующие ошибки:
![authentication_failed.png](images%2Fauthentication_failed.png)

3. Попробуем прослушать топик с заданием конфигурации пользователя следующей командой:

```shell
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --group test5 --from-beginning --consumer.config ../config/kraft/users/Alice_SASL.properties
```
В результате получается успешно прочитать сообщения:
![successful_authentication.png](images%2Fsuccessful_authentication.png)

## 3. Настройка авторизации

Для следующий пользователей зададим следующие права на работу с топиком test:
- Alice - чтение
- Tom - запись
- Bob - отсутствуют права

При помощи следующих команд добавим соответствующие права:

```shell
./kafka-acls.sh --bootstrap-server localhost:9092 --add --allow-principal User:Alice --operation Read --topic test --command-config ../config/kraft/users/Kafka_SASL.properties
./kafka-acls.sh --bootstrap-server localhost:9092 --add --allow-principal User:Tom --operation Write --topic test --command-config ../config/kraft/users/Kafka_SASL.properties

# Дополнительно добавим права на использование группы test
./kafka-acls.sh --bootstrap-server localhost:9092 --add --allow-principal User:Alice --operation Read --group test --command-config ../config/kraft/users/Kafka_SASL.properties
./kafka-acls.sh --bootstrap-server localhost:9092 --add --allow-principal User:Tom --operation Read --group test --command-config ../config/kraft/users/Kafka_SASL.properties
./kafka-acls.sh --bootstrap-server localhost:9092 --add --allow-principal User:Bob --operation Read --group test --command-config ../config/kraft/users/Kafka_SASL.properties
```

Результат выдачи прав:
![result_of_rights_issue.png](images%2Fresult_of_rights_issue.png)
![result_of_group_rights_issue.png](images%2Fresult_of_group_rights_issue.png)

Попробуем прочитать топик test каждым пользователем:
![read_authorization_results.png](images%2Fread_authorization_results.png)

Попробуем записать в топик какое либо сообщение:
![write_authorization_results.png](images%2Fwrite_authorization_results.png)





