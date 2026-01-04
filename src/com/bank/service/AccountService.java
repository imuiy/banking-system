package com.bank.service;

import com.bank.exception.BankingException;
import com.bank.model.*;
import com.bank.repository.*;
import java.sql.SQLException;
import java.util.List;

public class AccountService {
    private final AccountRepository accountRepo;
    private final UserRepository userRepo;
    private final AuditLogRepository auditLogRepo;
    
    public AccountService(AccountRepository accountRepo, UserRepository userRepo, 
                          AuditLogRepository auditLogRepo) {
        this.accountRepo = accountRepo;
        this.userRepo = userRepo;
        this.auditLogRepo = auditLogRepo;
    }
    
    public Account createAccount(String userId, AccountType type) throws BankingException {
        try {
            User user = userRepo.findById(userId);
            if (user == null) {
                throw new BankingException("User not found");
            }
            
            Account account = new Account(userId, type);
            accountRepo.save(account);
            auditLogRepo.save(new AuditLog("ACCOUNT_CREATED", userId, 
                "Created " + type + " account: " + account.getAccountNumber()));
            
            return account;
        } catch (SQLException e) {
            throw new BankingException("Account creation failed: " + e.getMessage());
        }
    }
    
    public void freezeAccount(String accountId, String adminUserId) throws BankingException {
        try {
            Account account = accountRepo.findById(accountId);
            if (account == null) {
                throw new BankingException("Account not found");
            }
            
            account.setStatus(AccountStatus.FROZEN);
            accountRepo.update(account);
            auditLogRepo.save(new AuditLog("ACCOUNT_FROZEN", adminUserId, 
                "Account frozen: " + account.getAccountNumber()));
        } catch (SQLException e) {
            throw new BankingException("Freeze account failed: " + e.getMessage());
        }
    }
    
    public void activateAccount(String accountId, String adminUserId) throws BankingException {
        try {
            Account account = accountRepo.findById(accountId);
            if (account == null) {
                throw new BankingException("Account not found");
            }
            
            account.setStatus(AccountStatus.ACTIVE);
            accountRepo.update(account);
            auditLogRepo.save(new AuditLog("ACCOUNT_ACTIVATED", adminUserId, 
                "Account activated: " + account.getAccountNumber()));
        } catch (SQLException e) {
            throw new BankingException("Activate account failed: " + e.getMessage());
        }
    }
    
    public List<Account> getUserAccounts(String userId) throws BankingException {
        try {
            return accountRepo.findByUserId(userId);
        } catch (SQLException e) {
            throw new BankingException("Failed to retrieve accounts: " + e.getMessage());
        }
    }
}