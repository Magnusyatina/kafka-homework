package com.magnusario.definitions.serializers;

import com.magnusario.definitions.LastPrice;
import lombok.SneakyThrows;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

public class LastPriceDeserializer extends JsonDeserializer<LastPrice> {

    @Override
    @SneakyThrows
    public LastPrice deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0)
            return null;
        return objectMapper.readValue(new BufferedInputStream(new ByteArrayInputStream(data)), LastPrice.class);
    }
}
