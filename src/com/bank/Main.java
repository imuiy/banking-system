package com.bank;

import com.bank.exception.BankingException;
import com.bank.model.*;
import com.bank.repository.*;
import com.bank.service.*;
import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        try {
            // initialize repositories
            UserRepository userRepo = new UserRepository();
            AccountRepository accountRepo = new AccountRepository();
            TransactionRepository transactionRepo = new TransactionRepository();
            AuditLogRepository auditLogRepo = new AuditLogRepository();
            
            // initialize services
            AuthService authService = new AuthService(userRepo, auditLogRepo);
            AccountService accountService = new AccountService(accountRepo, userRepo, auditLogRepo);
            TransactionService transactionService = new TransactionService(accountRepo, transactionRepo, auditLogRepo);

            FraudDetectionService fraudService = new FraudDetectionService(transactionRepo, auditLogRepo);
            System.out.println("=== ONLINE BANKING SYSTEM (SQL) ===\n");
            
            // register users
            User user1 = authService.register("John Doe", "john@email.com", "password123");
            User user2 = authService.register("Jane Smith", "jane@email.com", "password456");
            System.out.println("✓ Users registered\n");
            
            // create accounts
            Account acc1 = accountService.createAccount(user1.getId(), AccountType.CHECKING);
            Account acc2 = accountService.createAccount(user1.getId(), AccountType.SAVINGS);
            Account acc3 = accountService.createAccount(user2.getId(), AccountType.CHECKING);
            System.out.println("✓ Accounts created\n");
            
            // dep. money
            fraudService.analyzeTransaction(acc1.getId(), new BigDecimal("5000.00"), user1.getId());
            transactionService.deposit(acc1.getId(), new BigDecimal("5000.00"), user1.getId());

            fraudService.analyzeTransaction(acc3.getId(), new BigDecimal("3000.00"), user2.getId());
            transactionService.deposit(acc3.getId(), new BigDecimal("3000.00"), user2.getId());
            System.out.println("✓ Initial deposits made\n");
            
            // with. money
            fraudService.analyzeTransaction(acc1.getId(), new BigDecimal("500.00"), user1.getId());
            transactionService.withdraw(acc1.getId(), new BigDecimal("500.00"), user1.getId());
            System.out.println("✓ Withdrawal completed\n");
            
            // transfer between users (w database transaction)
            fraudService.analyzeTransaction(acc1.getId(), new BigDecimal("1000.00"), user1.getId());
            transactionService.transfer(acc1.getId(), acc3.getId(), new BigDecimal("1000.00"), user1.getId());
            System.out.println("✓ Transfer completed (ACID transaction)\n");
            
            // test fraud detec. with unusual transacs
            System.out.println("=== TESTING FRAUD DETECTION ===");
            fraudService.analyzeTransaction(acc1.getId(), new BigDecimal("50000.00"), user1.getId());
            System.out.println();

            // show results
            System.out.println("=== ACCOUNT BALANCES ===");
            for (Account acc : accountService.getUserAccounts(user1.getId())) {
                System.out.println(acc);
            }
            for (Account acc : accountService.getUserAccounts(user2.getId())) {
                System.out.println(acc);
            }
            
            System.out.println("\n=== TRANSACTION HISTORY (Account 1) ===");
            transactionService.getAccountHistory(acc1.getId()).forEach(System.out::println);
            
            System.out.println("\n=== AUDIT LOGS (Last 10) ===");
            auditLogRepo.findAll().stream().limit(10).forEach(System.out::println);
            
            // testing account freeze
            System.out.println("\n=== TESTING ACCOUNT FREEZE ===");
            accountService.freezeAccount(acc1.getId(), user1.getId());
            
            try {
                transactionService.withdraw(acc1.getId(), new BigDecimal("100.00"), user1.getId());
            } catch (BankingException e) {
                System.out.println("✓ Frozen account prevented transaction: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}