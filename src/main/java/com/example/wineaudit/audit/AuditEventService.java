package com.example.wineaudit.audit;

import com.example.wineaudit.events.DomainEvent;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class AuditEventService {

    private final AuditEventRepository repo;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    public AuditEventService(AuditEventRepository repo, JsonMapper jsonMapper, Clock clock) {
        this.repo = repo;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    @Transactional
    public void record(String key, DomainEvent<?> event) {
        String payloadJson = toJson(event);

        AuditEvent row = AuditEvent.of(
                event.eventId(),
                event.eventType(),
                key,
                event.requestId(),
                event.occurredAt(),
                Instant.now(clock),
                payloadJson
        );

        try {
            repo.save(row);
        } catch (DataIntegrityViolationException dup) {
            // Unique constraint on event_id => already recorded (idempotent)
        }
    }

    private String toJson(Object obj) {
        try {
            return jsonMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize audit event payload", e);
        }
    }
}
