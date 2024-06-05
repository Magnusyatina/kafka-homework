package org.magnusario;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.function.Consumer;

public class Utils {

    public static final Properties KAFKA_PRODUCER_CONFIG = new Properties() {{
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092,localhost:39092,localhost:49092");
        put(ProducerConfig.ACKS_CONFIG, "all");
    }};

    public static final Properties KAFKA_CONSUMER_CONFIG = new Properties() {{
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092,localhost:39092,localhost:49092");
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-1");
    }};

    public static Properties createProducerConfig(Consumer<Properties> propertiesAppender) {
        Properties currentProperties = (Properties) KAFKA_PRODUCER_CONFIG.clone();
        if (propertiesAppender != null)
            propertiesAppender.accept(currentProperties);
        return currentProperties;
    }

    public static Properties createProducerConfig() {
        return createProducerConfig(null);
    }

    public static Properties createConsumerConfig(Consumer<Properties> propertiesAppender) {
        Properties currentProperties = (Properties) KAFKA_CONSUMER_CONFIG.clone();
        if (propertiesAppender != null)
            propertiesAppender.accept(currentProperties);
        return currentProperties;
    }

    public static Properties createConsumerConfig() {
        return createConsumerConfig(null);
    }
}
