package com.magnusario.dataextractorbot;

import com.magnusario.definitions.LastPrice;
import com.magnusario.definitions.TradableShare;
import com.magnusario.definitions.serializers.ParametrizedJsonSerializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.tinkoff.piapi.core.InvestApi;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
public class DataExtractorBotApplication {

    @Value("${token}")
    private String token;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public static void main(String[] args) {
        SpringApplication.run(DataExtractorBotApplication.class, args);
    }

    @Bean
    public InvestApi investApi() {
        return InvestApi.create(token);
    }

    @Bean
    public NewTopic lastPriceTopic() {
        return new NewTopic("tradable-share-last-prices", Optional.empty(), Optional.empty());
    }

    @Bean
    public NewTopic tradableShareTopic() {
        return new NewTopic(
                "tradable-share",
                Optional.empty(),
                Optional.empty())
                .configs(new HashMap<>() {{
                    put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT);
                }});
    }

    @Bean
    public ProducerFactory<String, LastPrice> lastPriceProducerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ParametrizedJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, LastPrice> lastPriceKafkaTemplate() {
        return new KafkaTemplate<>(lastPriceProducerFactory());
    }

    @Bean
    public ProducerFactory<String, TradableShare> tradableShareProducerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ParametrizedJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, TradableShare> tradableShareKafkaTemplate() {
        return new KafkaTemplate<>(tradableShareProducerFactory());
    }

}
