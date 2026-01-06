package com.bank.service;

import com.bank.model.*;
import com.bank.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.*;

public class FraudDetectionService {
    private final TransactionRepository transactionRepo;
    private final AuditLogRepository auditLogRepo;

    public FraudDetectionService(TransactionRepository transactionRepo, AuditLogRepository auditLogRepo){
        this.transactionRepo = transactionRepo;
        this.auditLogRepo = auditLogRepo;
    }

    public void analyzeTransaction(String accountId, BigDecimal amount, String userId) 
            throws SQLException {
        
        List<Transaction> history = transactionRepo.findByAccountId(accountId);
        
        if (history.size() < 3) {
            return; // Not enough data
        }
        
        // Mean
        BigDecimal sum = BigDecimal.ZERO;
        for (Transaction t : history) {
            sum = sum.add(t.getAmount());
        }
        BigDecimal mean = sum.divide(new BigDecimal(history.size()), 2, RoundingMode.HALF_UP);
        
        // Standard dev
        BigDecimal variance = BigDecimal.ZERO;
        for (Transaction t : history) {
            BigDecimal diff = t.getAmount().subtract(mean);
            variance = variance.add(diff.multiply(diff));
        }
        variance = variance.divide(new BigDecimal(history.size()), 2, RoundingMode.HALF_UP);
        double stdDev = Math.sqrt(variance.doubleValue());
        
        // z-score
        double zScore = (amount.doubleValue() - mean.doubleValue()) / stdDev;
        
        // Flag if z-score > 2.5 
        if (Math.abs(zScore) > 2.5) {
            String alert = String.format(
                "ANOMALY_DETECTED: Transaction $%.2f deviates %.1f std devs from average $%.2f",
                amount, Math.abs(zScore), mean
            );
            
            auditLogRepo.save(new AuditLog("FRAUD_ALERT_ML", userId, alert));
            System.out.println("🤖 ML FRAUD ALERT: " + alert);
        }
    }
}
