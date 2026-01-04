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

    public AuditLog(String id, String action, String userId, String details, LocalDateTime timestamp) {
        this.id = id;
        this.action = action;
        this.userId = userId;
        this.details = details;
        this.timestamp = timestamp;
    }

    public String getId() {return id;}
    public String getAction() {return action;}
    public String getUserId() {return userId;}
    public String getDetails() {return details;}
    public LocalDateTime getTimestamp() {return timestamp;}

    @Override
    public String toString() {
        return String.format("[%s] %s by User %s: %s", 
            timestamp, action, userId, details);
    }
}
