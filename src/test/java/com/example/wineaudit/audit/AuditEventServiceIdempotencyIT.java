package com.example.wineaudit.audit;

import com.example.wineaudit.events.DomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false"
})
@ActiveProfiles("test")
class AuditEventServiceIdempotencyIT {

    @Autowired
    private AuditEventService service;

    @Autowired
    private AuditEventRepository repo;

    @Test
    void record_sameEventIdTwice_persistsOnlyOnce() {
        UUID eventId = UUID.randomUUID();

        DomainEvent<String> event = new DomainEvent<>(
                eventId,
                "WineryCreated",
                1,
                Instant.parse("2026-02-12T09:00:00Z"),
                "req-123",
                "{\"wineryId\":\"42\"}"
        );

        service.record("42", event);
        service.record("42", event);

        assertThat(repo.count()).isEqualTo(1);

        AuditEvent saved = repo.findAll().get(0);
        assertThat(saved.getEventId()).isEqualTo(eventId.toString());
        assertThat(saved.getEventType()).isEqualTo("WineryCreated");
        assertThat(saved.getAggregateId()).isEqualTo("42");
        assertThat(saved.getRequestId()).isEqualTo("req-123");
    }
}