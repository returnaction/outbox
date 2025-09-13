package com.nikita.server1.repository;

import com.nikita.model.outbox.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    @Query(value = "SELECT * FROM outbox_event WHERE status = :status ORDER BY created_at ASC LIMIT :limit", nativeQuery = true)
    List<OutboxEvent> findTopNByStatus(@Param("status") String status, @Param("limit") int limit);
}
