package com.bank.repository;

import com.bank.model.AuditLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogRepository {
    
    public void save(AuditLog log) throws SQLException {
        String sql = "INSERT INTO audit_logs (id, action, user_id, details) VALUES (?, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
            
        stmt.setString(1, log.getId());
        stmt.setString(2, log.getAction());
        stmt.setString(3, log.getUserId());
        stmt.setString(4, log.getDetails());
        
        stmt.executeUpdate();
        stmt.close();
    }
    
    public List<AuditLog> findAll() throws SQLException {
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC";
        List<AuditLog> logs = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                logs.add(new AuditLog(
                    rs.getString("id"),
                    rs.getString("action"),
                    rs.getString("user_id"),
                    rs.getString("details"),
                    rs.getTimestamp("timestamp").toLocalDateTime()
                ));
            }
        }
        return logs;
    }
    
    public List<AuditLog> findByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM audit_logs WHERE user_id = ? ORDER BY timestamp DESC";
        List<AuditLog> logs = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                logs.add(new AuditLog(
                    rs.getString("id"),
                    rs.getString("action"),
                    rs.getString("user_id"),
                    rs.getString("details"),
                    rs.getTimestamp("timestamp").toLocalDateTime()
                ));
            }
        }
        return logs;
    }
}