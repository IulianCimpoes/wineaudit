package com.example.wineaudit.audit;

public class DuplicateAuditEventException extends RuntimeException {
    public DuplicateAuditEventException(String message, Throwable cause) {
        super(message, cause);
    }
}