package com.magnusario.definitions.serdes;

import com.magnusario.definitions.LastPrice;
import com.magnusario.definitions.serializers.LastPriceDeserializer;
import com.magnusario.definitions.serializers.LastPriceSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class LastPriceSerde implements Serde<LastPrice> {
    @Override
    public Serializer<LastPrice> serializer() {
        return new LastPriceSerializer();
    }

    @Override
    public Deserializer<LastPrice> deserializer() {
        return new LastPriceDeserializer();
    }
}
