package com.nikita.server1.config;

import com.nikita.model.order.OrderDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfigVariant2 {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    @Bean("kafkaTemplateVariant2")
    public KafkaTemplate<String, OrderDto> kafkaTemplate() {
        DefaultKafkaProducerFactory<String, OrderDto> pf = new DefaultKafkaProducerFactory<>(producerFactoryProps());
        pf.setTransactionIdPrefix("tx-variant2-");
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public Map<String, Object> producerFactoryProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "tx-variant2");
        return props;
    }
}
