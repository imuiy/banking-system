package com.bank.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class Account {
    private final String id;
    private final String userId;
    private final String accountNumber;
    private AccountType type;
    private BigDecimal balance;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private final ReentrantLock lock;

    public Account(String userId, AccountType type){
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.accountNumber = generateAccountNumber();
        this.type = type;
        this.balance = BigDecimal.ZERO;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.lock = new ReentrantLock();
    }

    private String generateAccountNumber(){
        return String.format("%012d", (long)(Math.random() * 1000000000000L));
    }

    public void lock(){
        lock.lock();
    }

    public void unlock(){
        lock.unlock();
    }

    //getters
    public String getId() {return id;}
    public String getUserId() {return userId;}
    public String getAccountNumber() {return accountNumber;}
    public AccountType getType() {return type;}
    public BigDecimal getBalance() {return balance;}
    public AccountStatus getStatus() {return status;}

    //setters
    public void setBalance(BigDecimal balance) {this.balance = balance;}
    public void setStatus(AccountStatus status) {this.status = status;}

    @Override
    public String toString(){
        return String.format("Account[%s, Type: %s, Balance: $%.2f, Status: %s]",
            accountNumber, type, balance, status);
    }
}
