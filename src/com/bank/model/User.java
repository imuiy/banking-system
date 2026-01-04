package com.bank.model;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Base64;

public class User {
    private final String id;
    private String name;
    private String email;
    private String passwordHash;
    private String salt;
    private Role role;
    private LocalDateTime createdAt;

    //new users
    public User(String name, String email, String password, Role role){
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.salt = generateSalt();
        this.passwordHash = hashPassword(password, salt);
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    //constructor loading from database
    public User(String id, String name, String email, String passwordHash, String salt, Role role){
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    private String generateSalt(){
        SecureRandom rand = new SecureRandom();
        byte[] salt = new byte[16];
        rand.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public boolean verifyPassword(String password){
        return hashPassword(password, salt).equals(passwordHash);
    }

    //getters
    public String getId() {return id;}
    public String getName() {return name;}
    public String getEmail() {return email;}
    public Role getRole() {return role;}
}
