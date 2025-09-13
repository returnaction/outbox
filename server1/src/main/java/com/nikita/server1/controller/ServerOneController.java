package com.nikita.server1.controller;

import com.nikita.model.order.OrderDto;
import com.nikita.model.order.OrderRequestDto;
import com.nikita.server1.service.ServerOneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/server1")
@RequiredArgsConstructor
public class ServerOneController {

    private final ServerOneService serverOneService;

    @PostMapping("/variant1")
    public ResponseEntity<OrderDto> createOrder1(@RequestBody OrderRequestDto request) {
        return serverOneService.createOrder1(request);
    }

    @PostMapping("/variant2")
    public ResponseEntity<OrderDto> createOrder2(@RequestBody OrderRequestDto request) {
        return serverOneService.createOrder2(request);
    }

    @PostMapping("/variant3")
    public ResponseEntity<OrderDto> createOrder3(@RequestBody OrderRequestDto request) {
        return serverOneService.createOrder3(request);
    }

}
