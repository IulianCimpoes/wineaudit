package com.example.wineaudit.kafka;

import com.example.wineaudit.audit.AuditEventService;
import com.example.wineaudit.events.DomainEvent;
import tools.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

@Component
public class WineCellarEventsListener {

    private final AuditEventService service;
    private final JsonMapper jsonMapper;

    public WineCellarEventsListener(AuditEventService service, JsonMapper jsonMapper) {
        this.service = service;
        this.jsonMapper = jsonMapper;
    }

    @KafkaListener(topics = "${wineaudit.kafka.topic}")
    public void onMessage(ConsumerRecord<String, String> record) {
        DomainEvent<JsonNode> event = parse(record.value());

        if (event.requestId() != null) {
            MDC.put("requestId", event.requestId());
        }

        try {
            service.record(record.key(), event);
        } finally {
            MDC.remove("requestId");
        }
    }

    private DomainEvent<JsonNode> parse(String json) {
        try {
            JavaType payloadType = jsonMapper.getTypeFactory()
                                             .constructParametricType(DomainEvent.class, JsonNode.class);

            return jsonMapper.readValue(json, payloadType);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize DomainEvent JSON", e);
        }
    }
}