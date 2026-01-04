package com.bank.repository;

import com.bank.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    
    public void save(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (id, from_account_id, to_account_id, amount, type, description) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transaction.getId());
            stmt.setString(2, transaction.getFromAccountId());
            stmt.setString(3, transaction.getToAccountId());
            stmt.setBigDecimal(4, transaction.getAmount());
            stmt.setString(5, transaction.getType().name());
            stmt.setString(6, transaction.getDescription());
            
            stmt.executeUpdate();
        }
    }
    
    public List<Transaction> findByAccountId(String accountId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE from_account_id = ? OR to_account_id = ? ORDER BY timestamp DESC";
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, accountId);
            stmt.setString(2, accountId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getString("id"),
                    rs.getString("from_account_id"),
                    rs.getString("to_account_id"),
                    rs.getBigDecimal("amount"),
                    TransactionType.valueOf(rs.getString("type")),
                    rs.getString("description"),
                    rs.getTimestamp("timestamp").toLocalDateTime()
                ));
            }
        }
        return transactions;
    }
}