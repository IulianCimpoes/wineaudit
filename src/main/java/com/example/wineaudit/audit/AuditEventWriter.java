package com.example.wineaudit.audit;

import com.example.wineaudit.events.DomainEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;

@Component
public class AuditEventWriter {

    private final AuditEventRepository repo;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    public AuditEventWriter(AuditEventRepository repo, JsonMapper jsonMapper, Clock clock) {
        this.repo = repo;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insert(String key, DomainEvent<?> event) {
        AuditEvent row = AuditEvent.of(
                event.eventId(),
                event.eventType(),
                key,
                event.requestId(),
                event.occurredAt(),
                Instant.now(clock),
                toJson(event)
        );

        try {
            repo.saveAndFlush(row); // forces constraint check inside this method
        } catch (DataIntegrityViolationException dup) {
            // IMPORTANT: throw so Spring rolls back cleanly (no commit attempt)
            throw new DuplicateAuditEventException("Duplicate eventId=" + event.eventId(), dup);
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