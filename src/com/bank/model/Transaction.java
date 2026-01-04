package com.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {
    private final String id;
    private final String fromAccountId;
    private final String toAccountId;
    private final BigDecimal amount;
    private final TransactionType type;
    private final LocalDateTime timestamp;
    private final String description;

    public Transaction(String fromAccountId, String toAccountId, BigDecimal amount, TransactionType type, String description){
        this.id = UUID.randomUUID().toString();
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    public Transaction(String id ,String fromAccountId, String toAccountId, BigDecimal amount, TransactionType type, String description, LocalDateTime timestamp){
        this.id = id;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.description = description;
    }

    //getters
    public String getId() {return id;}
    public String getFromAccountId() {return fromAccountId;}
    public String getToAccountId() {return toAccountId;}
    public BigDecimal getAmount() {return amount;}
    public TransactionType getType() {return type;}
    public LocalDateTime getTimestamp() {return timestamp;}
    public String getDescription() {return description;}

    @Override
    public String toString() {
        return String.format("[%s] %s: $%.2f - %s", 
            timestamp, type, amount, description);
    }
}
