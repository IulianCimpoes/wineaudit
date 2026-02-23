package com.example.wineaudit.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "audit_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEvent {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "event_id", length = 36, nullable = false)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_id", length = 36)
    private String aggregateId;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "occurred_at")
    private Instant occurredAt;

    @Column(name = "consumed_at", nullable = false)
    private Instant consumedAt;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    public static AuditEvent of(UUID eventId,
                                String eventType,
                                String aggregateId,
                                String requestId,
                                Instant occurredAt,
                                Instant consumedAt,
                                String payloadJson) {
        AuditEvent e = new AuditEvent();
        e.id = UUID.randomUUID().toString();
        e.eventId = eventId.toString();
        e.eventType = eventType;
        e.aggregateId = aggregateId;
        e.requestId = requestId;
        e.occurredAt = occurredAt;
        e.consumedAt = consumedAt;
        e.payload = payloadJson;
        return e;
    }
}
