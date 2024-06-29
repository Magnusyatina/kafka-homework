package com.magnusario.definitions.serdes;

import com.magnusario.definitions.aggregators.PriceAggregator;
import com.magnusario.definitions.serializers.ParametrizedJsonDeserializer;
import com.magnusario.definitions.serializers.ParametrizedJsonSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class PriceAggregatorSerde implements Serde<PriceAggregator> {

    @Override
    public Serializer<PriceAggregator> serializer() {
        return new ParametrizedJsonSerializer<>();
    }

    @Override
    public Deserializer<PriceAggregator> deserializer() {
        return new ParametrizedJsonDeserializer<>(PriceAggregator.class);
    }
}
