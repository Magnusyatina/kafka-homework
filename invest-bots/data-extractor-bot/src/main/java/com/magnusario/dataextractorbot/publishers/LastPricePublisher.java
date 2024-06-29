package com.magnusario.dataextractorbot.publishers;

import com.magnusario.definitions.LastPrice;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.temporal.TemporalField;

@Component
public class LastPricePublisher implements MarketInformationPublisher<LastPrice> {

    @Autowired
    private KafkaTemplate<String, LastPrice> kafkaTemplate;

    @Override
    public void publish(LastPrice value) {
        ProducerRecord<String, LastPrice> producerRecord = new ProducerRecord<>(
                "tradable-share-last-prices",
                null,
                value.getZonedDateTime().toInstant().toEpochMilli(),
                value.getFigi(),
                value);
        kafkaTemplate.send("tradable-share-last-prices", value.getFigi(), value);
    }
}
