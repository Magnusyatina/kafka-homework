package com.magnusario.definitions.serializers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public abstract class JsonDeserializer<T> implements Deserializer<T> {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    public JsonDeserializer() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Deserializer.super.configure(configs, isKey);
    }

}
