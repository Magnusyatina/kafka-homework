package com.magnusario;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;
import java.util.function.Consumer;

public class Utils {

    public static final Properties properties = new Properties() {{
        put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092,localhost:39092,localhost:49092");
        put(StreamsConfig.APPLICATION_ID_CONFIG, "event-count-app");
        put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    }};

    public static Properties createStreamConfig() {
        return createStreamConfig(null);
    }

    public static Properties createStreamConfig(Consumer<Properties> consumer) {
        Properties currentProperties = (Properties) properties.clone();
        if (consumer != null)
            consumer.accept(currentProperties);
        return currentProperties;
    }
}
