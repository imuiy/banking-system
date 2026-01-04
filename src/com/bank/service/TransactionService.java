package com.bank.service;

import com.bank.exception.BankingException;
import com.bank.model.*;
import com.bank.repository.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class TransactionService {
    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;
    private final AuditLogRepository auditLogRepo;
    
    public TransactionService(AccountRepository accountRepo, 
                              TransactionRepository transactionRepo,
                              AuditLogRepository auditLogRepo) {
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
        this.auditLogRepo = auditLogRepo;
    }
    
    public void deposit(String accountId, BigDecimal amount, String userId) 
            throws BankingException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Amount must be positive");
        }
        
        try {
            Account account = accountRepo.findById(accountId);
            if (account == null) {
                throw new BankingException("Account not found");
            }
            
            if (account.getStatus() != AccountStatus.ACTIVE) {
                throw new BankingException("Account is not active");
            }
            
            account.setBalance(account.getBalance().add(amount));
            accountRepo.update(account);
            
            Transaction transaction = new Transaction(null, accountId, amount, 
                TransactionType.DEPOSIT, "Deposit to account");
            transactionRepo.save(transaction);
            
            auditLogRepo.save(new AuditLog("DEPOSIT", userId, 
                String.format("Deposited $%.2f to %s", amount, account.getAccountNumber())));
        } catch (SQLException e) {
            throw new BankingException("Deposit failed: " + e.getMessage());
        }
    }
    
    public void withdraw(String accountId, BigDecimal amount, String userId) 
            throws BankingException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Amount must be positive");
        }
        
        try {
            Account account = accountRepo.findById(accountId);
            if (account == null) {
                throw new BankingException("Account not found");
            }
            
            if (account.getStatus() != AccountStatus.ACTIVE) {
                throw new BankingException("Account is not active");
            }
            
            if (account.getBalance().compareTo(amount) < 0) {
                throw new BankingException("Insufficient funds");
            }
            
            account.setBalance(account.getBalance().subtract(amount));
            accountRepo.update(account);
            
            Transaction transaction = new Transaction(accountId, null, amount, 
                TransactionType.WITHDRAWAL, "Withdrawal from account");
            transactionRepo.save(transaction);
            
            auditLogRepo.save(new AuditLog("WITHDRAWAL", userId, 
                String.format("Withdrew $%.2f from %s", amount, account.getAccountNumber())));
        } catch (SQLException e) {
            throw new BankingException("Withdrawal failed: " + e.getMessage());
        }
    }
    
    public void transfer(String fromAccountId, String toAccountId, 
                        BigDecimal amount, String userId) throws BankingException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION
            
            Account fromAccount = accountRepo.findById(fromAccountId);
            Account toAccount = accountRepo.findById(toAccountId);
            
            if (fromAccount == null || toAccount == null) {
                throw new BankingException("Account not found");
            }
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BankingException("Amount must be positive");
            }
            
            if (fromAccount.getStatus() != AccountStatus.ACTIVE || 
                toAccount.getStatus() != AccountStatus.ACTIVE) {
                throw new BankingException("Both accounts must be active");
            }
            
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new BankingException("Insufficient funds");
            }
            
            // Update balances
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));
            
            accountRepo.update(fromAccount);
            accountRepo.update(toAccount);
            
            // Save transaction
            Transaction transaction = new Transaction(fromAccountId, toAccountId, amount, 
                TransactionType.TRANSFER, 
                String.format("Transfer from %s to %s", 
                    fromAccount.getAccountNumber(), toAccount.getAccountNumber()));
            transactionRepo.save(transaction);
            
            // Save audit log
            auditLogRepo.save(new AuditLog("TRANSFER", userId, 
                String.format("Transferred $%.2f from %s to %s", 
                    amount, fromAccount.getAccountNumber(), toAccount.getAccountNumber())));
            
            conn.commit(); // COMMIT TRANSACTION
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // ROLLBACK ON ERROR
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new BankingException("Transfer failed: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public List<Transaction> getAccountHistory(String accountId) throws BankingException {
        try {
            Account account = accountRepo.findById(accountId);
            if (account == null) {
                throw new BankingException("Account not found");
            }
            return transactionRepo.findByAccountId(accountId);
        } catch (SQLException e) {
            throw new BankingException("Failed to retrieve history: " + e.getMessage());
        }
    }
}