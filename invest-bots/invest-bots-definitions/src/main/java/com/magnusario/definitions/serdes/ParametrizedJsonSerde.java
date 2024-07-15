package com.magnusario.definitions.serdes;

import com.magnusario.definitions.serializers.ParametrizedJsonDeserializer;
import com.magnusario.definitions.serializers.ParametrizedJsonSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class ParametrizedJsonSerde<T> implements Serde<T> {

    private final Class<T> targetClass;

    public ParametrizedJsonSerde(Class<T> targetClass) {
        this.targetClass = targetClass;
    }


    @Override
    public Serializer<T> serializer() {
        return new ParametrizedJsonSerializer<>();
    }

    @Override
    public Deserializer<T> deserializer() {
        return new ParametrizedJsonDeserializer<>(targetClass);
    }
}
