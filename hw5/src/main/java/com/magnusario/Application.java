package com.magnusario;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.SessionWindows;

import java.time.Duration;

public class Application {

    public static void main(String[] args) throws InterruptedException {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, Long> eventsCountStream = streamsBuilder.stream("events")
                .map((key, value) -> {
                    return new KeyValue<>(key, value);
                })
                .groupByKey()
                .windowedBy(SessionWindows.ofInactivityGapWithNoGrace(Duration.ofMinutes(5)))
                .count(Materialized.as("event-counts-store"))
                .toStream()
                .map((key, value) -> new KeyValue<>(key.key().toString(), value));

        eventsCountStream.to("events-count-by-key", Produced.with(Serdes.String(), Serdes.Long()));
        eventsCountStream.foreach((key, value) -> System.out.println("key: %s with count: %s".formatted(key, value)));
        Topology topology = streamsBuilder.build();
        TopologyDescription describe = topology.describe();
        System.out.println(describe);
        try (KafkaStreams kafkaStreams = new KafkaStreams(topology, Utils.createStreamConfig())){
            kafkaStreams.start();
            Thread.sleep(3600000);
        }
    }
}
