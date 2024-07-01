package com.magnusario.signals.monitor;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.magnusario.definitions.Signal;
import com.magnusario.definitions.bots.KafkaStreamBot;
import com.magnusario.definitions.notifications.InvestNotification;
import com.magnusario.definitions.notifications.InvestmentForecast;
import com.magnusario.definitions.serdes.ParametrizedJsonSerde;
import com.magnusario.definitions.serdes.SignalSerde;
import lombok.Data;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.SlidingWindows;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class SignalsBot extends KafkaStreamBot {

    private static final Logger logger = LoggerFactory.getLogger(SignalsBot.class);

    private static final String APPLICATION_ID = "signals-stream-bot-monitor";
    public static final String SOURCE_TOPIC = "signals";
    public static final int MIN_LIMITATION_OF_SIGNALS_WEIGHT = 1;
    public static final String NOTIFICATIONS_TOPIC = "notifications";

    @Autowired
    public SignalsBot(KafkaProperties kafkaProperties) {
        super(kafkaProperties);
    }

    @Override
    protected Properties buildProperties(KafkaProperties kafkaProperties) {
        Map<String, Object> streamsProperties = kafkaProperties.buildStreamsProperties(new DefaultSslBundleRegistry());
        streamsProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        streamsProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        streamsProperties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SignalSerde.class);
        Properties properties = new Properties();
        properties.putAll(streamsProperties);
        return properties;
    }

    @Override
    protected Topology buildTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, Signal> stream = streamsBuilder.stream(SOURCE_TOPIC);
        stream
                .groupByKey()
                .windowedBy(SlidingWindows.ofTimeDifferenceAndGrace(Duration.ofMinutes(15), Duration.ofMinutes(2)))
                .aggregate(SignalWeightAggregator::new,
                        ((key, value, aggregate) -> aggregate.addSignal(key, value)), formSignalsWeightStore())
                .filter(((key, value) -> value.getTotalWeights() >= MIN_LIMITATION_OF_SIGNALS_WEIGHT))
                .toStream()
                .peek(((key, value) -> logger.info(STR."Form notification for figi \{key.key()}, because the weight of the signals exceeded the permissible threshold \{MIN_LIMITATION_OF_SIGNALS_WEIGHT}. Current weight sum = \{value.getTotalWeights()}")))
                .map(((key, value) -> new KeyValue<>(key.key(), formNotification(value))))
                .to(NOTIFICATIONS_TOPIC, Produced.with(Serdes.String(), new ParametrizedJsonSerde<>(InvestNotification.class)));
        return streamsBuilder.build();
    }

    private InvestNotification formNotification(SignalWeightAggregator value) {
        return new InvestNotification(value.getKey(), InvestmentForecast.BUY, value.getLastSignalBySource().values().stream().toList());
    }

    private Materialized<String, SignalWeightAggregator, WindowStore<Bytes, byte[]>> formSignalsWeightStore() {
        Materialized<String, SignalWeightAggregator, WindowStore<Bytes, byte[]>> objectObjectStateStoreMaterialized
                = Materialized.as("signals-weight-store");
        objectObjectStateStoreMaterialized = objectObjectStateStoreMaterialized.withKeySerde(Serdes.String())
                .withValueSerde(new ParametrizedJsonSerde<>(SignalWeightAggregator.class))
                .withStoreType(Materialized.StoreType.ROCKS_DB);
        return objectObjectStateStoreMaterialized;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SignalWeightAggregator {

        private String key;

        private Map<String, Signal> lastSignalBySource = new HashMap<>();

        public SignalWeightAggregator addSignal(String key, Signal signal) {
            if (this.key == null)
                this.key = key;
            String signalSourceKey = STR."\{key}-\{signal.getSignalSource()}";
            lastSignalBySource.put(signalSourceKey, signal);
            return this;
        }

        public Integer getTotalWeights() {
            return lastSignalBySource
                    .values()
                    .stream()
                    .mapToInt(Signal::getWeight)
                    .sum();
        }
    }
}
