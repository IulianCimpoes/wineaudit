package com.example.wineaudit.kafka;

import com.example.wineaudit.audit.AuditEventService;
import com.example.wineaudit.events.DomainEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class WineCellarEventsListener {

    private final AuditEventService service;
    private final String topic;

    public WineCellarEventsListener(AuditEventService service,
                                    @Value("${wineaudit.kafka.topic}") String topic) {
        this.service = service;
        this.topic = topic;
    }

    @KafkaListener(topics = "${wineaudit.kafka.topic}")
    public void onMessage(ConsumerRecord<String, DomainEvent<?>> record) {
        // Correlation: requestId from payload - upgrade to headers later
        DomainEvent<?> event = record.value();
        if (event != null && event.requestId() != null) {
            MDC.put("requestId", event.requestId());
        }

        try {
            service.record(record.key(), event);
        } finally {
            MDC.remove("requestId");
        }
    }
}
