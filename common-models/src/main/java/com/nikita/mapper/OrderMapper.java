package com.nikita.mapper;

import com.nikita.model.order.OrderDto;
import com.nikita.model.order.OrderEntity;
import com.nikita.model.order.OrderRequestDto;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderDto toDto(OrderEntity entity) {
        return OrderDto.builder()
                .orderId(entity.getOrderId())
                .quantity(entity.getQuantity())
                .sku(entity.getSku())
                .build();
    }

    public OrderEntity toEntity(OrderRequestDto dto) {
        return OrderEntity.builder()
                .sku(dto.getSku())
                .quantity(dto.getQuantity())
                .build();
    }

    public OrderEntity toEntity(OrderDto dto){
        return OrderEntity.builder()
                .orderId(dto.getOrderId())
                .quantity(dto.getQuantity())
                .sku(dto.getSku())
                .build();
    }
}
