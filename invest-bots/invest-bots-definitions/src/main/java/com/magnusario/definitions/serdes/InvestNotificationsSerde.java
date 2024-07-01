package com.magnusario.definitions.serdes;

import com.magnusario.definitions.notifications.InvestNotification;
import com.magnusario.definitions.serializers.ParametrizedJsonDeserializer;
import com.magnusario.definitions.serializers.ParametrizedJsonSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class InvestNotificationsSerde implements Serde<InvestNotification> {
    @Override
    public Serializer<InvestNotification> serializer() {
        return new ParametrizedJsonSerializer<>();
    }

    @Override
    public Deserializer<InvestNotification> deserializer() {
        return new ParametrizedJsonDeserializer<>(InvestNotification.class);
    }
}
