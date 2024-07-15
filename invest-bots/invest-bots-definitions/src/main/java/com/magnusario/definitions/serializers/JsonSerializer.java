package com.magnusario.definitions.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public abstract class JsonSerializer<T> implements Serializer<T> {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    public JsonSerializer() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }
}
