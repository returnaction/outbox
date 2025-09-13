package com.nikita.server2.service;

import com.nikita.mapper.OrderMapper;
import com.nikita.model.order.OrderDto;
import com.nikita.model.order.OrderEntity;
import com.nikita.server2.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;


    @Transactional(readOnly = true)
    public ResponseEntity<OrderDto> getOrderById(UUID orderId) {
        OrderEntity orderDto = orderRepository.findById(orderId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ " + orderId + " не найден"));
        OrderDto dto = orderMapper.toDto(orderDto);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Transactional
    public void createOrder(OrderDto order) {
        OrderEntity entity = orderMapper.toEntity(order);
        orderRepository.save(entity);
    }
}
