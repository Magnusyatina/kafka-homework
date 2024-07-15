package com.magnusario.definitions.serdes;

import com.magnusario.definitions.LastPrice;
import org.apache.kafka.common.serialization.Serde;

public class ApplicationSerdes {

    public static Serde<LastPrice> lastPriceSerde() {
        return new LastPriceSerde();
    }

    public static <T> Serde<T> parametrizedJsonSerde(Class<T> targetClass) {
        return new ParametrizedJsonSerde<>(targetClass);
    }

}
