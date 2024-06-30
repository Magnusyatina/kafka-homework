package com.magnusario.dataextractorbot.publishers;

import com.magnusario.definitions.LastPrice;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class LastPricePublisher implements MarketInformationPublisher<LastPrice> {

    public static final String TOPIC_DESTINATION = "tradable-share-last-prices";

    @Autowired
    private KafkaTemplate<String, LastPrice> lastPriceKafkaTemplate;

    @Override
    public void publish(LastPrice value) {
        long eventTime = value.getZonedDateTime().toInstant().toEpochMilli();
        String messageKey = value.getFigi();
        ProducerRecord<String, LastPrice> producerRecord = new ProducerRecord<>(
                TOPIC_DESTINATION,
                null,
                eventTime,
                messageKey,
                value);
        lastPriceKafkaTemplate.send(producerRecord);
    }
}
