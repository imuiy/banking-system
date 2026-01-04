package com.bank.repository;

import com.bank.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRepository {
    
    public void save(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (id, user_id, account_number, type, balance, status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, account.getId());
            stmt.setString(2, account.getUserId());
            stmt.setString(3, account.getAccountNumber());
            stmt.setString(4, account.getType().name());
            stmt.setBigDecimal(5, account.getBalance());
            stmt.setString(6, account.getStatus().name());
            
            stmt.executeUpdate();
        }
    }
    
    public Account findById(String accountId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
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
    
    public List<Account> findByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        List<Account> accounts = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(new Account(
                    rs.getString("id"),
                    rs.getString("user_id"),
                    rs.getString("account_number"),
                    AccountType.valueOf(rs.getString("type")),
                    rs.getBigDecimal("balance"),
                    AccountStatus.valueOf(rs.getString("status"))
                ));
            }
        }
        return accounts;
    }
    
    public void update(Account account) throws SQLException {
        String sql = "UPDATE accounts SET balance = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, account.getBalance());
            stmt.setString(2, account.getStatus().name());
            stmt.setString(3, account.getId());
            
            stmt.executeUpdate();
        }
    }
}