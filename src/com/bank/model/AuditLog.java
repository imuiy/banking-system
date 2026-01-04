package com.bank.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditLog {
    private final String id;
    private final String action;
    private final String userId;
    private final String details;
    private final LocalDateTime timestamp;

    public AuditLog(String action, String userId, String details) {
        this.id = UUID.randomUUID().toString();
        this.action = action;
        this.userId = userId;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s by User %s: %s", 
            timestamp, action, userId, details);
    }
}
