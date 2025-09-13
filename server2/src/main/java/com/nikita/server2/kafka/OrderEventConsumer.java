package com.nikita.server2.kafka;

import com.nikita.model.order.OrderDto;
import com.nikita.server2.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "order-events", groupId = "server1-group")
    public void consume(OrderDto order){
        orderService.createOrder(order);
    }

}
