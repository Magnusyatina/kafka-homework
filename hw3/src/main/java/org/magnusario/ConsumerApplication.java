package org.magnusario;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class ConsumerApplication {

    public static void main(String[] args) {
        List<String> topics = Arrays.asList("topic1", "topic2");
        ConsumerRecords<String, String> records = null;
        try (KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(Utils.createConsumerConfig(p -> {
            p.put(ConsumerConfig.GROUP_ID_CONFIG, "non-transactional-group");
            p.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_uncommitted");
        }))) {
            System.out.println("NON TRANSACTIONAL CONSUMER\n---------------------------------");
            kafkaConsumer.subscribe(topics);
            while (!(records = kafkaConsumer.poll(Duration.ofSeconds(10))).isEmpty()) {
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("%s: %s".formatted(record.topic(), record.value()));
                }
            }
        }

        try (KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(Utils.createConsumerConfig(p -> {
            p.put(ConsumerConfig.GROUP_ID_CONFIG, "transactional-group");
            p.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        }))) {
            System.out.println("TRANSACTIONAL CONSUMER\n---------------------------------");
            kafkaConsumer.subscribe(topics);
            while (!(records = kafkaConsumer.poll(Duration.ofSeconds(10))).isEmpty()) {
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("%s: %s".formatted(record.topic(), record.value()));
                }
            }
        }
    }
}
