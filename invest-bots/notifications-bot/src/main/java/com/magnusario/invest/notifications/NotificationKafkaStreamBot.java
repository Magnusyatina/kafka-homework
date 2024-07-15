package com.magnusario.invest.notifications;

import com.magnusario.definitions.Signal;
import com.magnusario.definitions.TradableShare;
import com.magnusario.definitions.bots.KafkaStreamBot;
import com.magnusario.definitions.notifications.InvestNotification;
import com.magnusario.definitions.notifications.InvestmentForecast;
import com.magnusario.definitions.notifications.SignalsNotification;
import com.magnusario.definitions.notifications.TradableShareNotification;
import com.magnusario.definitions.serdes.InvestNotificationsSerde;
import com.magnusario.definitions.serdes.ParametrizedJsonSerde;
import com.magnusario.invest.notifications.channels.NotificationPublisher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Component
public class NotificationKafkaStreamBot extends KafkaStreamBot {

    private static final Logger logger = LoggerFactory.getLogger(NotificationKafkaStreamBot.class);

    private static final String APPLICATION_ID = "notifications-stream-bot-monitor";
    public static final String NOTIFICATIONS_TOPIC = "notifications";
    public static final String ACTUAL_NOTIFICATIONS_STORE_NAME = "actual-notifications";
    public static final String TRADABLE_SHARE_TOPIC = "tradable-share";

    @Autowired
    private NotificationPublisher notificationPublisher;

    public NotificationKafkaStreamBot(KafkaProperties kafkaProperties) {
        super(kafkaProperties);
    }

    @Override
    protected Properties buildProperties(KafkaProperties kafkaProperties) {
        Map<String, Object> streamsProperties = kafkaProperties.buildStreamsProperties(new DefaultSslBundleRegistry());
        streamsProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        streamsProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        streamsProperties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, InvestNotificationsSerde.class);
        Properties properties = new Properties();
        properties.putAll(streamsProperties);
        return properties;
    }

    @Override
    protected Topology buildTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        GlobalKTable<String, TradableShare> tradableShareTable =
                streamsBuilder.globalTable(
                        TRADABLE_SHARE_TOPIC,
                        Consumed.with(Serdes.String(), new ParametrizedJsonSerde<>(TradableShare.class)));
        KStream<String, InvestNotification> stream = streamsBuilder.stream(NOTIFICATIONS_TOPIC);
        stream
                .groupByKey()
                .reduce((notification1, notification2) -> notification2, formNotificationsStore())
                .toStream()
                .leftJoin(tradableShareTable, ((key, value) -> key), createTradableShareJoiner())
                .peek(((key, value) -> logger.info("Received notification for figi {}. Investment forecast is {}", value.getFigi(), value.getInvestmentForecast())))
                .foreach(((key, value) -> notificationPublisher.publish(value)));
        return streamsBuilder.build();
    }

    private ValueJoiner<InvestNotification, TradableShare, ComplexNotification> createTradableShareJoiner() {
        return (investNotification, tradableShare) -> ComplexNotification
                .builder()
                .figi(investNotification.getFigi())
                .ticker(tradableShare.getTicker())
                .currency(tradableShare.getCurrency())
                .lastCheckingTime(tradableShare.getLastCheckingTime())
                .investmentForecast(investNotification.getInvestmentForecast())
                .signals(new HashSet<>(investNotification.getSignals()))
                .build();
    }


    private Materialized<String, InvestNotification, KeyValueStore<Bytes, byte[]>> formNotificationsStore() {
        Materialized<String, InvestNotification, KeyValueStore<Bytes, byte[]>> materializedStore
                = Materialized.as(ACTUAL_NOTIFICATIONS_STORE_NAME);
        return materializedStore
                .withKeySerde(Serdes.String())
                .withValueSerde(new ParametrizedJsonSerde<>(InvestNotification.class));
    }

    @Builder
    @AllArgsConstructor
    @Data
    public static class ComplexNotification implements TradableShareNotification, SignalsNotification {
        private final String figi;
        private final String ticker;
        private final String currency;
        private final ZonedDateTime lastCheckingTime;
        private final InvestmentForecast investmentForecast;
        private final Set<Signal> signals;
    }
}
