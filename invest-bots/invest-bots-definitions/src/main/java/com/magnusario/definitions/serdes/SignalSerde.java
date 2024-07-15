package com.magnusario.definitions.serdes;

import com.magnusario.definitions.Signal;
import com.magnusario.definitions.serializers.ParametrizedJsonDeserializer;
import com.magnusario.definitions.serializers.ParametrizedJsonSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class SignalSerde implements Serde<Signal> {
    @Override
    public Serializer<Signal> serializer() {
        return new ParametrizedJsonSerializer<>();
    }

    @Override
    public Deserializer<Signal> deserializer() {
        return new ParametrizedJsonDeserializer<>(Signal.class);
    }
}
