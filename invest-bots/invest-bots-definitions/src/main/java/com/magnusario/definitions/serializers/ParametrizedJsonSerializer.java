package com.magnusario.definitions.serializers;

import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;

public class ParametrizedJsonSerializer<T> extends JsonSerializer<T> {
    @Override
    @SneakyThrows
    public byte[] serialize(String topic, T data) {
        return objectMapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
    }
}
