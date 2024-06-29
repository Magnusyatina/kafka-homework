package com.magnusario.trade.monitor;

import com.magnusario.definitions.LastPrice;
import com.magnusario.definitions.Signal;
import com.magnusario.definitions.aggregators.PriceAggregator;
import com.magnusario.definitions.bots.KafkaStreamBot;
import com.magnusario.definitions.serdes.ApplicationSerdes;
import com.magnusario.definitions.serdes.LastPriceSerde;
import com.magnusario.definitions.serdes.PriceAggregatorSerde;
import com.magnusario.trade.monitor.config.SignalsConfiguration;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;

@Component
public class UnexpectedPriceByAverageStreamBot extends KafkaStreamBot {
    public static final String AVERAGE_PRICES_STORE_NAME = "average-prices-store";
    public static final String TRADABLE_SHARE_LAST_PRICES_TOPIC_NAME = "tradable-share-last-prices";
    public static final String SIGNALS_TOPIC_NAME = "signals";
    public static final String APPLICATION_ID = "asset-price-streams";
    private final SignalsConfiguration signalsConfiguration;

    @Autowired
    public UnexpectedPriceByAverageStreamBot(KafkaProperties kafkaProperties, SignalsConfiguration signalsConfiguration) {
        super(kafkaProperties);
        if (signalsConfiguration.getMeta() == null || signalsConfiguration.getMeta().isEmpty())
            throw new IllegalStateException("Не задана конфигурация сигналов");
        this.signalsConfiguration = signalsConfiguration;
    }

    @Override
    protected Properties buildProperties(KafkaProperties kafkaProperties) {
        Map<String, Object> streamsProperties = kafkaProperties.buildStreamsProperties(new DefaultSslBundleRegistry());
        streamsProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        streamsProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        streamsProperties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, LastPriceSerde.class);
        Properties properties = new Properties();
        properties.putAll(streamsProperties);
        return properties;
    }

    @Override
    protected Topology buildTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, LastPrice> assetPrices = streamsBuilder.stream(TRADABLE_SHARE_LAST_PRICES_TOPIC_NAME);
        for (SignalsConfiguration.SignalMeta signalMeta : signalsConfiguration.getMeta()) {
            KTable<String, BigDecimal> averagePrices = assetPrices
                    .groupByKey()
                    .aggregate(
                            () -> new PriceAggregator(signalMeta.getCleanInterval()),
                            (key, value, aggregate) -> aggregate.addLastPrice(key, value),
                            formPriceAggregatorStore(signalMeta))
                    .mapValues(PriceAggregator::getAverage);

            assetPrices.join(averagePrices, (price, average) -> checkMaxLimitations(price, average, signalMeta.getPercentLimitation()))
                    .filter(((key, value) -> value.isPriceIncreaseLimitReached()))
                    .map(((key, value) -> new KeyValue<>(key, formSignal(key, value, signalMeta))))
                    .to(SIGNALS_TOPIC_NAME, Produced.with(Serdes.String(), ApplicationSerdes.parametrizedJsonSerde(Signal.class)));
        }
        return streamsBuilder.build();
    }

    private Materialized<String, PriceAggregator, KeyValueStore<Bytes, byte[]>> formPriceAggregatorStore(SignalsConfiguration.SignalMeta signalMeta) {
        Materialized<String, PriceAggregator, KeyValueStore<Bytes, byte[]>> objectObjectStateStoreMaterialized
                = Materialized.as(STR."\{AVERAGE_PRICES_STORE_NAME}-CI\{signalMeta.getCleanInterval()}-PL\{signalMeta.getPercentLimitation()}-W\{signalMeta.getWeight()}");
        objectObjectStateStoreMaterialized = objectObjectStateStoreMaterialized.withKeySerde(Serdes.String())
                .withValueSerde(new PriceAggregatorSerde())
                .withStoreType(Materialized.StoreType.ROCKS_DB);
        return objectObjectStateStoreMaterialized;
    }

    private Signal formSignal(String key, MaxLimitationsInfo value, SignalsConfiguration.SignalMeta signalMeta) {
        return new Signal(key, STR."""
                    Резкое повышение цены более чем на \{signalMeta.getPercentLimitation()}%.
                    Средняя цена: \{value.getAveragePrice()}, Текущая цена: \{value.getLastPrice().getPrice()}
                    """, signalMeta.getWeight(), STR."\{APPLICATION_ID}-CI\{signalMeta.getCleanInterval()}-PL\{signalMeta.getPercentLimitation()}-W\{signalMeta.getWeight()}");
    }


    private MaxLimitationsInfo checkMaxLimitations(LastPrice price, BigDecimal average, @NotNull Double percentLimitation) {
        double currentPrice = price.getPrice().doubleValue();
        double averagePrice = average.doubleValue();
        return new MaxLimitationsInfo(
                price,
                averagePrice,
                averagePrice > 0.0
                        && currentPrice > averagePrice * (1 + percentLimitation / 100.0));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MaxLimitationsInfo {
        private LastPrice lastPrice;
        private double averagePrice;
        private boolean priceIncreaseLimitReached;
    }
}
