package com.example.wineaudit.audit;

import com.example.wineaudit.events.DomainEvent;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.*;

class AuditEventServiceTest {

    @Test
    void record_savesAuditRow() {
        AuditEventRepository repo = mock(AuditEventRepository.class);
        JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();
        Clock clock = Clock.fixed(Instant.parse("2026-02-12T10:00:00Z"), ZoneOffset.UTC);

        AuditEventService service = new AuditEventService(repo, mapper, clock);

        DomainEvent<String> event = new DomainEvent<>(
                UUID.randomUUID(),
                "WineryCreated",
                1,
                Instant.parse("2026-02-12T09:00:00Z"),
                "req-123",
                "{\"wineryId\":\"42\"}"
        );

        service.record("42", event);

        verify(repo, times(1)).save(any(AuditEvent.class));
    }

    @Test
    void record_duplicateEventId_doesNotThrow() {
        AuditEventRepository repo = mock(AuditEventRepository.class);
        JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();
        Clock clock = Clock.systemUTC();

        AuditEventService service = new AuditEventService(repo, mapper, clock);

        DomainEvent<String> event = new DomainEvent<>(
                UUID.randomUUID(),
                "WineryCreated",
                1,
                Instant.now(),
                "req-123",
                "{}"
        );

        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(repo).save(any(AuditEvent.class));

        service.record("42", event); // should not throw
    }
}