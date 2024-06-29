package com.magnusario.definitions.serializers;

import com.magnusario.definitions.LastPrice;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;

public class LastPriceSerializer extends JsonSerializer<LastPrice> {
    @Override
    @SneakyThrows
    public byte[] serialize(String topic, LastPrice data) {
        return objectMapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
    }

}
