package com.nikita.server1.kafka;

import com.nikita.model.order.OrderDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderEventPublisher {

    private static final String TOPIC = "order-events";

    @Qualifier("kafkaTemplateVariant1")
    private final KafkaTemplate<String, OrderDto> kafkaTemplateVariant1;

    @Qualifier("kafkaTemplateVariant2")
    private final KafkaTemplate<String, OrderDto> kafkaTemplateVariant2;

    public OrderEventPublisher(@Qualifier("kafkaTemplateVariant1") KafkaTemplate<String, OrderDto> kafkaTemplateVariant1,
                               @Qualifier("kafkaTemplateVariant2") KafkaTemplate<String, OrderDto> kafkaTemplateVariant2) {
        this.kafkaTemplateVariant1 = kafkaTemplateVariant1;
        this.kafkaTemplateVariant2 = kafkaTemplateVariant2;
    }

    public CompletableFuture<SendResult<String, OrderDto>> publishVariant1(OrderDto order) {
        CompletableFuture<SendResult<String, OrderDto>> send = kafkaTemplateVariant1.send(TOPIC, order.getOrderId().toString(), order);
        return send;
    }

    public void publishVariant2(OrderDto order){
        kafkaTemplateVariant2.executeInTransaction(kt -> {
            kt.send(TOPIC, order.getOrderId().toString(), order);
            return true;
        });
    }

    public CompletableFuture<SendResult<String, OrderDto>> publishVariant3(OrderDto order) {
        return kafkaTemplateVariant1.send(TOPIC, order.getOrderId().toString(), order);
    }
}
