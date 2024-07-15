# Домашнее задание №6. Развернуть Kafka Connect, настроить интеграцию с PostgreSQL, используя Debezium PostgreSQL CDC Source Connector

1. Описание конфигурации сервисов в docker-compose.yml:
   - Кластер из 3 брокеров Kafka
   - Kafka UI
   - Kafka Connect
   - База данных Postgresql


+ Postgresql

```yaml
  postgres:
    image: postgres:latest
    restart: unless-stopped
    environment:
      POSTGRES_USERNAME: postgres
      POSTGRES_PASSWORD: 12345
    volumes:
      - ./postgresql/data:/var/lib/postgresql/data
      - ./postgresql/dumps-script:/docker-entrypoint-initdb.d
      - ./postgresql/dumps:/tmp/pg
    ports:
      - "5432:5432"
```
Для создания базы данных, созданием базы данных и наполнением ее данными на этапе первого запуска используются скрипты из
директорий postgresql/dumps-script и postgresql/dumps

+ Kafka Connect:

```yaml
  kafka-connect:
    image: confluentinc/cp-kafka-connect:latest
    depends_on:
      - kafka1
      - kafka2
      - kafka3
    ports:
      - "8083:8083"
    environment:
      CONNECT_BOOTSTRAP_SERVERS: "kafka1:9092,kafka2:9092,kafka3:9092"
      CONNECT_REST_PORT: "8083"
      CONNECT_GROUP_ID: "local-connect"
      CONNECT_CONFIG_STORAGE_TOPIC: "local-connect-config"
      CONNECT_OFFSET_STORAGE_TOPIC: "local-connect-offsets"
      CONNECT_STATUS_STORAGE_TOPIC: "local-connect-status"
      CONNECT_KEY_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_VALUE_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_INTERNAL_KEY_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_INTERNAL_VALUE_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_REST_ADVERTISED_HOST_NAME: "localhost"
      CONNECT_LOG4J_ROOT_LOGLEVEL: "INFO"
      CONNECT_PLUGIN_PATH: /usr/share/java/,/etc/kafka-connect/jars
    volumes:
      - ./connect-plugins:/etc/kafka-connect/jars
```
В качестве внешнего раздела подключается директория, содержащая необходимые jar файлы для работы плагина 
Debezium PostgreSQL CDC Source Connector, в нее так же можно добавлять любые другие плагины.

+ Kafka:

```yaml
  kafka1:
    image: confluentinc/cp-kafka:latest
    ports:
      - 59092:59092
    environment:
      CLUSTER_ID: q1Sh-9_ISia_zwGINzRvyQ
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_NODE_ID: 1
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka1:9093,2@kafka2:9093,3@kafka3:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENERS: PLAINTEXT://:9092,PLAINTEXT_HOST://:59092,CONTROLLER://:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka1:9092,PLAINTEXT_HOST://localhost:59092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT,CONTROLLER:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
      KAFKA_DEFAULT_REPLICATION_FACTOR: 3
      KAFKA_NUM_PARTITIONS: 3
...
```

+ Kafka UI:

```yaml
  kafka-ui:
    container_name: kafka-ui-test
    image: provectuslabs/kafka-ui:latest
    restart: unless-stopped
    ports:
      - 8999:8080
    environment:
      DYNAMIC_CONFIG_ENABLED: 'true'
    volumes:
      - ./kui/config.yml:/etc/kafkaui/dynamic_config.yaml
```

Полная конфигурация представлена в файле [docker-compose.yml](docker-compose.yml)


2. Дополнительная настройка:

Для корректной работы коннектора необходимо настроить журнал предзаписи и настроить параметр wal_level, задав ему значение logical,
в данном режиме в журнал будут записываться данные для пподдержки архивирования WAL и репликации, информацию, необходимую для восстановления
после сбоя и информацию, требующуюся для поддержки логического декодирования. Настраивается этот режим в файле **postgresql.conf**:
![configure_postgresql_conf.png](images%2Fconfigure_postgresql_conf.png)


3. Первый запуск:

Для запуска всех сервисов используется команда:
```shell
docker compose up -d --build
```

После успешного запуска всех сервисов в kafka должны появиться служебные топики, созданные kafka-connect:
![kafka_connect_topics.png](images%2Fkafka_connect_topics.png)

Сам коннектор должен отдавать соответствующий ответ по API http://localhost:8083 с информацией о кластере kafka:
![kafka-connect-status.png](images%2Fkafka-connect-status.png)

При обращении к http://localhost:8083/connector-plugins должен выдаваться список установленных плагинов kafka-connect:
![kafka-connect-plugins.png](images%2Fkafka-connect-plugins.png)


4. Настройка и запуск нового коннектора Debezium PostgreSQL CDC Source Connector, который будет отсылать данные в топик kafka из заданных таблиц.
Для этого потребуется описать файл с конфигурацией нового коннектора:
```json
{
  "name": "logs-connector",
  "config": {
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "database.hostname": "postgres",
    "database.port": "5432",
    "database.user": "postgres",
    "database.password": "12345",
    "database.dbname" : "archive",
    "topic.prefix": "postgres",
    "table.include.list": "public.logs",
    "plugin.name": "pgoutput"
  }
}
```
[clients.json](clients.json)
В качестве параметров задается класс, используемого коннектора и данные для подключения к базе данных, а так же таблицы, из которых будет происходить считывание данных
и конечный топик, куда будут попадать данные.

Добавляется новый коннектор при помощи команды:
```shell
curl -X POST --data-binary "@clients.json" -H "Content-Type: application/json" http://localhost:8083/connectors | jq
```
![result_of_adding_a_new_connector.png](images%2Fresult_of_adding_a_new_connector.png)

Статус работы коннектора можно посмотреть при помощи команды:
```shell
curl http://localhost:8083/connectors/logs-connector/status | jq
```
![connector_operation_status.png](images%2Fconnector_operation_status.png)

После начала работы коннектора и захвата данных, в kafka будет создан новый топик, репрезентирующий просматриваемую таблицу:
![topic_from_table_created.png](images%2Ftopic_from_table_created.png)


5. Результаты работы коннектора:

В базе данных было изначально сгенерировано 1000 записей в таблице logs:
![database_archive_entry.png](images%2Fdatabase_archive_entry.png)

При запуске коннектора все записи были пересланы в соответствующий топик при помощи коннектора:
![connector_operation_result.png](images%2Fconnector_operation_result.png)

Каждое сообщение содержит информацию о записи в базе данных в формате AVRO:
![message_body_in_topic.png](images%2Fmessage_body_in_topic.png)

При добавлении новой записи в базу данных, в топике kafka появится новое сообщение:
![insert_new_record_in_database.png](images%2Finsert_new_record_in_database.png)
![insert_new_record_in_database_kafka_result.png](images%2Finsert_new_record_in_database_kafka_result.png)

При обновлении старой записи в базе данных, в топике kafka так же появится новое сообщение:
![update_record_in_database.png](images%2Fupdate_record_in_database.png)

При удалении записи из базы данных, в топике kafka появится два новых сообщения:
![delete_record_in_database.png](images%2Fdelete_record_in_database.png)
![delete_record_in_database_kafka_result.png](images%2Fdelete_record_in_database_kafka_result.png)













