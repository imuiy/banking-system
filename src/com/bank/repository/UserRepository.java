package com.bank.repository;

import com.bank.model.*;
import java.sql.*;

public class UserRepository {
    
    public void save(User user) throws SQLException {
        String sql = "INSERT INTO users (id, name, email, password_hash, salt, role) VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
            
        stmt.setString(1, user.getId());
        stmt.setString(2, user.getName());
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getPasswordHash());
        stmt.setString(5, user.getSalt());
        stmt.setString(6, user.getRole().name());
        
        stmt.executeUpdate();
        stmt.close();
    }
    
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("salt"),
                    Role.valueOf(rs.getString("role"))
                );
            }
        }
        return null;
    }
    
    public User findById(String userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("salt"),
                    Role.valueOf(rs.getString("role"))
                );
            }
        }
        return null;
    }
}