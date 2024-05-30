package org.magnusario;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.concurrent.ExecutionException;

public class ProducerApplication {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(Utils.createProducerConfig(p -> p.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "hw3")))) {
            kafkaProducer.initTransactions();
            kafkaProducer.beginTransaction();
            kafkaProducer.send(new ProducerRecord<>("topic1", "My first message in transaction"));
            kafkaProducer.send(new ProducerRecord<>("topic1", "My second message in transaction"));
            kafkaProducer.send(new ProducerRecord<>("topic1", "My third message in transaction"));
            kafkaProducer.send(new ProducerRecord<>("topic1", "My fourth message in transaction"));
            kafkaProducer.send(new ProducerRecord<>("topic1", "My fifth message in transaction"));
            kafkaProducer.send(new ProducerRecord<>("topic2", "My first message in transaction"));
            kafkaProducer.send(new ProducerRecord<>("topic2", "My second message in transaction"));
            kafkaProducer.send(new ProducerRecord<>("topic2", "My third message in transaction"));
            kafkaProducer.send(new ProducerRecord<>("topic2", "My fourth message in transaction"));
            kafkaProducer.send(new ProducerRecord<>("topic2", "My fifth message in transaction"));
            kafkaProducer.commitTransaction();
            kafkaProducer.beginTransaction();
            kafkaProducer.send(new ProducerRecord<>("topic1", "My first message in failed transaction"));
            kafkaProducer.send(new ProducerRecord<>("topic1", "My second message in failed transaction"));
            kafkaProducer.send(new ProducerRecord<>("topic2", "My first message in failed transaction"));
            kafkaProducer.send(new ProducerRecord<>("topic2", "My second message in failed transaction"));
            Thread.sleep(5000);
            kafkaProducer.abortTransaction();
            Thread.sleep(5000);
        }
    }
}
