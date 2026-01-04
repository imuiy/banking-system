package com.bank.service;

import com.bank.exception.BankingException;
import com.bank.model.*;
import com.bank.repository.*;
import java.sql.SQLException;

public class AuthService {
    private final UserRepository userRepo;
    private final AuditLogRepository auditLogRepo;
    
    public AuthService(UserRepository userRepo, AuditLogRepository auditLogRepo) {
        this.userRepo = userRepo;
        this.auditLogRepo = auditLogRepo;
    }
    
    public User register(String name, String email, String password) throws BankingException {
        try {
            if (userRepo.findByEmail(email) != null) {
                throw new BankingException("Email already registered");
            }
            
            User user = new User(name, email, password, Role.USER);
            userRepo.save(user);
            auditLogRepo.save(new AuditLog("USER_REGISTERED", user.getId(), 
                "New user registered: " + email));
            
            return user;
        } catch (SQLException e) {
            throw new BankingException("Registration failed: " + e.getMessage());
        }
    }
    
    public User login(String email, String password) throws BankingException {
        try {
            User user = userRepo.findByEmail(email);
            if (user == null || !user.verifyPassword(password)) {
                throw new BankingException("Invalid credentials");
            }
            
            auditLogRepo.save(new AuditLog("USER_LOGIN", user.getId(), 
                "User logged in: " + email));
            return user;
        } catch (SQLException e) {
            throw new BankingException("Login failed: " + e.getMessage());
        }
    }
}