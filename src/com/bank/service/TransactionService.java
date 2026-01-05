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
        
        // Fetch accounts without auto-closing connection
        Account fromAccount = findAccountById(conn, fromAccountId);
        Account toAccount = findAccountById(conn, toAccountId);
        
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
        
        updateAccount(conn, fromAccount);
        updateAccount(conn, toAccount);
        
        // Save transaction
        Transaction transaction = new Transaction(fromAccountId, toAccountId, amount, 
            TransactionType.TRANSFER, 
            String.format("Transfer from %s to %s", 
                fromAccount.getAccountNumber(), toAccount.getAccountNumber()));
        saveTransaction(conn, transaction);
        
        // Save audit log
        AuditLog log = new AuditLog("TRANSFER", userId, 
            String.format("Transferred $%.2f from %s to %s", 
                amount, fromAccount.getAccountNumber(), toAccount.getAccountNumber()));
        saveAuditLog(conn, log);
        
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
                // Connection already closed, ignore
            }
        }
    }
}

    // Helper methods to use the same connection
    private Account findAccountById(Connection conn, String accountId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Account(
                    rs.getString("id"),
                    rs.getString("user_id"),
                    rs.getString("account_number"),
                    AccountType.valueOf(rs.getString("type")),
                    rs.getBigDecimal("balance"),
                    AccountStatus.valueOf(rs.getString("status"))
                );
            }
        }
        return null;
    }

    private void updateAccount(Connection conn, Account account) throws SQLException {
        String sql = "UPDATE accounts SET balance = ?, status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, account.getBalance());
            stmt.setString(2, account.getStatus().name());
            stmt.setString(3, account.getId());
            stmt.executeUpdate();
        }
    }

    private void saveTransaction(Connection conn, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (id, from_account_id, to_account_id, amount, type, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, transaction.getId());
            stmt.setString(2, transaction.getFromAccountId());
            stmt.setString(3, transaction.getToAccountId());
            stmt.setBigDecimal(4, transaction.getAmount());
            stmt.setString(5, transaction.getType().name());
            stmt.setString(6, transaction.getDescription());
            stmt.executeUpdate();
        }
    }

    private void saveAuditLog(Connection conn, AuditLog log) throws SQLException {
        String sql = "INSERT INTO audit_logs (id, action, user_id, details) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, log.getId());
            stmt.setString(2, log.getAction());
            stmt.setString(3, log.getUserId());
            stmt.setString(4, log.getDetails());
            stmt.executeUpdate();
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