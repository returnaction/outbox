package com.nikita.server1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikita.mapper.OrderMapper;
import com.nikita.model.order.OrderDto;
import com.nikita.model.order.OrderEntity;
import com.nikita.model.order.OrderRequestDto;
import com.nikita.model.outbox.EventStatus;
import com.nikita.model.outbox.EventType;
import com.nikita.model.outbox.OutboxEvent;
import com.nikita.server1.kafka.OrderEventPublisher;
import com.nikita.server1.repository.OrderRepository;
import com.nikita.server1.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServerOneService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher orderEventPublisher;
    private final OutboxRepository outboxRepository;

    @Transactional
    public ResponseEntity<OrderDto> createOrder1(OrderRequestDto request) {
        OrderEntity entity = orderMapper.toEntity(request);
        entity.setOrderId(UUID.randomUUID());
        OrderEntity saved = orderRepository.save(entity);
        OrderDto dto = orderMapper.toDto(saved);
        try {
            orderEventPublisher.publishVariant1(dto).join();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка Kafka variant1, откат транзакции", e);
        }
        return new ResponseEntity<>(dto, HttpStatus.CREATED);

        /**
         🔹 Шпаргалка: Вариант 1 — try/catch + CompletableFuture.join()
         Что делаем:
         Сохраняем заказ в БД через orderRepository.save() внутри @Transactional.
         Отправляем событие в Kafka через kafkaTemplate.send(...).join().
         join() блокирует поток до завершения отправки.
         Если Kafka упадёт, выбрасывается CompletionException (unchecked).
         Транзакция откатится автоматически при ошибке Kafka, так как Spring откатывает транзакции на unchecked-исключениях.
         Плюсы:
         Простой код, легко понять.
         Ошибка Kafka → транзакция откатывается.
         Минусы:
         Нет глобальной атомарности:
         Если Kafka примет сообщение до commit транзакции, а JVM упадёт → сервер 2 может получить событие, которого нет в БД.
         Поток блокируется на время отправки Kafka.
         */
    }

    @Transactional
    public ResponseEntity<OrderDto> createOrder2(OrderRequestDto request) {
        OrderEntity entity = orderMapper.toEntity(request);
        entity.setOrderId(UUID.randomUUID());

        OrderEntity saved = orderRepository.save(entity);
        OrderDto dto = orderMapper.toDto(saved);

        orderEventPublisher.publishVariant2(dto);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);

        /**
         🔹 Шпаргалка: Вариант 2 — Kafka Transactions + @Transactional Hibernate
         Что делаем
         Создаём отдельный KafkaTemplate с transactionIdPrefix для включения транзакций Kafka.
         Сохраняем заказ в БД внутри метода с @Transactional (Hibernate/JPA транзакция).
         Вызываем kafkaTemplate.executeInTransaction(...) для отправки сообщения в Kafka.
         Если что-то падает в блоке Kafka → транзакция Kafka abort, выбрасывается исключение → Hibernate транзакция откатывается.
         Если ошибка произойдёт после commit Kafka → БД транзакция может откатиться, а сообщение уже ушло в Kafka → возможен рассинхрон.
         🔹 Настройка ChainedTransactionManager устарела и не рекомендуется, поэтому здесь мы работаем без него.
         Плюсы
         «Псевдо-атомарность» DB + Kafka для большинства практических задач.
         Нет рассинхронов, если ошибка происходит до commit Kafka.
         Полностью современный и поддерживаемый способ (без устаревшего ChainedTransactionManager).
         Минусы
         Полной атомарности DB + Kafka нет: если БД откатится после commit Kafka → может быть рассинхрон.
         Сложнее, чем обычная отправка сообщений.
         Поток блокируется до завершения транзакции Kafka в executeInTransaction.

         🔹 Схема последовательности
         Открывается транзакция Hibernate через @Transactional.
         Создаётся объект OrderEntity и сохраняется через orderRepository.save().
         KafkaTemplate.executeInTransaction начинает транзакцию Kafka.
         Сообщение отправляется в Kafka (в рамках Kafka tx).
         Возможные сценарии:
         Всё ок → commit Kafka tx → commit Hibernate tx (DB).
         Ошибка внутри executeInTransaction → abort Kafka tx → rollback Hibernate tx.
         Ошибка после commit Kafka → DB транзакция откатится, сообщение в Kafka уже ушло → возможен рассинхрон.         */
    }

    @Transactional
    public ResponseEntity<OrderDto> createOrder3(OrderRequestDto request) {
        /// СОХРАНЯЕМ ЗАКАЗ
        OrderEntity entity = orderMapper.toEntity(request);
        entity.setOrderId(UUID.randomUUID());
        OrderEntity saved = orderRepository.save(entity);
        OrderDto dto = orderMapper.toDto(saved);

        /// СОХРАНЯЕМ СОБЫТИЕ В OUTBOX
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .orderId(dto.getOrderId())
                .payload(dto)
                .eventType(EventType.ORDER_CREATED)
                .status(EventStatus.NEW)
                .build();

        outboxRepository.save(event);
        // Регистрируем синхронизацию после коммита
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderEventPublisher.publishVariant3(dto)
                        .whenComplete((result, exception) -> {
                            if (exception == null) {
                                // Кафка успешно приняло сообщение
                                event.setStatus(EventStatus.SENT);
                                outboxRepository.save(event);
                            } else {
                                System.out.println("Не удалось отправить событие в Kafka: \n" + exception.getMessage());
                            }
                        });
            }
        });

        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }
}
