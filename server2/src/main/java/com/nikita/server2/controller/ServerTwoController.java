package com.nikita.server2.controller;

import com.nikita.model.order.OrderDto;
import com.nikita.server2.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/server2")
@RequiredArgsConstructor
public class ServerTwoController {

    private final OrderService orderService;

    @GetMapping("{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable UUID orderId) {
        return orderService.getOrderById(orderId);
    }
}
