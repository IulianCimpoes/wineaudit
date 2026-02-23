CREATE TABLE audit_event (
                             id VARCHAR(36) PRIMARY KEY,
                             event_id VARCHAR(36) NOT NULL,
                             event_type VARCHAR(100) NOT NULL,
                             aggregate_id VARCHAR(36),
                             request_id VARCHAR(100),
                             occurred_at TIMESTAMP,
                             consumed_at TIMESTAMP NOT NULL,
                             payload CLOB NOT NULL
);

CREATE UNIQUE INDEX ux_audit_event_event_id ON audit_event(event_id);
