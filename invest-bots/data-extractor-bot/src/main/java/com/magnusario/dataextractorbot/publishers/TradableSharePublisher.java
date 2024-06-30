package com.magnusario.dataextractorbot.publishers;

import com.magnusario.definitions.TradableShare;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TradableSharePublisher implements MarketInformationPublisher<TradableShare> {

    public static final String TOPIC_DESTINATION = "tradable-share";

    @Autowired
    private KafkaTemplate<String, TradableShare> tradableShareKafkaTemplate;

    @Override
    public void publish(TradableShare value) {
        long eventTime = value.getLastCheckingTime().toInstant().toEpochMilli();
        String messageKey = value.getFigi();
        ProducerRecord<String, TradableShare> producerRecord = new ProducerRecord<>(
                TOPIC_DESTINATION,
                null,
                eventTime,
                messageKey,
                value);
        tradableShareKafkaTemplate.send(producerRecord);
    }
}
