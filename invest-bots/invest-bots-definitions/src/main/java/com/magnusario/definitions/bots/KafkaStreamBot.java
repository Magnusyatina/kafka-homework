package com.magnusario.definitions.bots;

import jakarta.annotation.PreDestroy;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.Properties;

@EnableScheduling
public abstract class KafkaStreamBot {

    private static final Logger logger = LoggerFactory.getLogger(KafkaStreamBot.class);

    private KafkaProperties kafkaProperties;
    protected KafkaStreams kafkaStreams;

    protected boolean started;

    public KafkaStreamBot(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Scheduled(fixedDelay = 10000)
    public void start() {
        if (started)
            return;
        logger.info("Starting kafka bot");
        Topology topology = buildTopology();
        kafkaStreams = new KafkaStreams(topology, buildProperties(kafkaProperties));
        System.out.println(topology.describe());
        kafkaStreams.start();
        started = true;
        logger.info(STR."Topology created: \n\{topology.describe().toString()}");
        logger.info("Kafka bot started");
    }

    protected Properties buildProperties(KafkaProperties kafkaProperties) {
        Map<String, Object> streamsProperties = kafkaProperties.buildStreamsProperties(new DefaultSslBundleRegistry());
        Properties properties = new Properties();
        properties.putAll(streamsProperties);
        return properties;
    }

    protected abstract Topology buildTopology();

    @PreDestroy
    public void destroy() {
        if (kafkaStreams != null)
            kafkaStreams.close();
    }
}
