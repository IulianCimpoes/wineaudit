package com.example.wineaudit.audit;

import com.example.wineaudit.events.DomainEvent;
import org.springframework.stereotype.Service;

@Service
public class AuditEventService {

    private final AuditEventWriter writer;

    public AuditEventService(AuditEventWriter writer) {
        this.writer = writer;
    }

    public void record(String key, DomainEvent<?> event) {
        try {
            writer.insert(key, event);
        } catch (DuplicateAuditEventException ignored) {
            // idempotent: already recorded
        }
    }
}