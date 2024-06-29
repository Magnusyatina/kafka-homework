package com.magnusario.definitions.serializers;

import lombok.SneakyThrows;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

public class ParametrizedJsonDeserializer<T> extends JsonDeserializer<T> {

    private final Class<T> targetClass;

    public ParametrizedJsonDeserializer(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    @SneakyThrows
    public T deserialize(String topic, byte[] data) {
        return objectMapper.readValue(new BufferedInputStream(new ByteArrayInputStream(data)), targetClass);
    }
}
