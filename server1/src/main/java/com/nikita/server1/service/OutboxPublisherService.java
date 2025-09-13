package com.nikita.server1.service;

import com.nikita.model.order.OrderDto;
import com.nikita.model.outbox.EventStatus;
import com.nikita.model.outbox.OutboxEvent;
import com.nikita.server1.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxPublisherService {

    private final OutboxRepository outboxRepository;
    private final @Qualifier("kafkaTemplateVariant1") KafkaTemplate<String, OrderDto> kafkaTemplate1;

    public OutboxPublisherService(OutboxRepository outboxRepository, @Qualifier("kafkaTemplateVariant1") KafkaTemplate<String, OrderDto> kafkaTemplate1) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate1 = kafkaTemplate1;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishOutboxEvents() {
        List<OutboxEvent> events = outboxRepository.findTopNByStatus("NEW", 10);

        for (OutboxEvent event : events) {
            OrderDto payload = event.getPayload();
            kafkaTemplate1.send("order-events", event.getOrderId().toString(), payload)
                    .whenComplete((result, exception) -> {
                        if (exception == null) {
                            event.setStatus(EventStatus.SENT);
                            outboxRepository.save(event);
                        } else {
                            System.out.println("Не удалось отправить событие id={" + event.getId() + "} : " + exception.getMessage());
                        }
                    });
        }

    }

}

