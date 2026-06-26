package com.gdb.transactions.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        seedTransactionLogs();
    }

    private void seedTransactionLogs() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transaction_logging", Integer.class);
        if (count == null || count < 10) {
            log.info("Transaction log count is {}. Reseeding a rich set of dummy logs...", count);

            jdbcTemplate.update("DELETE FROM transaction_logging");
            jdbcTemplate.update("DELETE FROM fund_transfers");
            jdbcTemplate.update("ALTER SEQUENCE transaction_logging_id_seq RESTART WITH 1");
            jdbcTemplate.update("ALTER SEQUENCE fund_transfers_id_seq RESTART WITH 1");

            // --- 1001 (John Doe - Savings) ---
            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '10 days')
                """, 1001L, new BigDecimal("60000.00"), "DEPOSIT", null, "Initial Cash Deposit", "CASH", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '7 days')
                """, 1001L, new BigDecimal("5000.00"), "WITHDRAW", null, "ATM Cash Withdrawal", "ATM", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '6 days')
                """, 1001L, new BigDecimal("2500.00"), "DEPOSIT", null, "Cash Deposit", "CASH", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '1 day')
                """, 1001L, new BigDecimal("8000.00"), "DEPOSIT", null, "ATM Cash Deposit", "ATM", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '12 hours')
                """, 1001L, new BigDecimal("1200.00"), "WITHDRAW", null, "Online Debit Payment", "ONLINE", "SUCCESS");

            // --- 1002 (System Admin - Current) ---
            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '9 days')
                """, 1002L, new BigDecimal("150000.00"), "DEPOSIT", null, "Initial Cash Deposit", "CASH", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '4 days')
                """, 1002L, new BigDecimal("20000.00"), "WITHDRAW", null, "Branch Cash Withdrawal", "BRANCH", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '3 days')
                """, 1002L, new BigDecimal("45000.00"), "DEPOSIT", null, "Online Deposit", "ONLINE", "SUCCESS");

            // --- 1003 (Teller User - Savings) ---
            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '10 days')
                """, 1003L, new BigDecimal("80000.00"), "DEPOSIT", null, "Salary Credit", "ONLINE", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '5 days')
                """, 1003L, new BigDecimal("4500.00"), "WITHDRAW", null, "ATM Cash Out", "ATM", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '2 days')
                """, 1003L, new BigDecimal("2000.00"), "WITHDRAW", null, "Grocery Purchase", "ONLINE", "SUCCESS");

            // --- 1004 (Manager User - Current) ---
            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '8 days')
                """, 1004L, new BigDecimal("200000.00"), "DEPOSIT", null, "Capital Funding", "ONLINE", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '3 days')
                """, 1004L, new BigDecimal("15000.00"), "WITHDRAW", null, "Office Supplies", "BRANCH", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '1 day')
                """, 1004L, new BigDecimal("8000.00"), "DEPOSIT", null, "Client Fee Payment", "ONLINE", "SUCCESS");

            // --- Transfers ---
            // 1. Transfer 1: 1002 to 1001 (reference_id = 1)
            jdbcTemplate.update("""
                INSERT INTO fund_transfers (from_account, to_account, transfer_amount, transfer_mode, created_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '8 days')
                """, 1002L, 1001L, new BigDecimal("10000.00"), "IMPS");
            
            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '8 days')
                """, 1002L, new BigDecimal("10000.00"), "TRANSFER", 1L, "Fund Transfer to 1001", "IMPS", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '8 days')
                """, 1001L, new BigDecimal("10000.00"), "TRANSFER", 1L, "Fund Transfer from 1002", "IMPS", "SUCCESS");

            // 2. Transfer 2: 1001 to 1002 (reference_id = 2)
            jdbcTemplate.update("""
                INSERT INTO fund_transfers (from_account, to_account, transfer_amount, transfer_mode, created_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '5 days')
                """, 1001L, 1002L, new BigDecimal("15000.00"), "NEFT");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '5 days')
                """, 1001L, new BigDecimal("15000.00"), "TRANSFER", 2L, "Fund Transfer to 1002", "NEFT", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '5 days')
                """, 1002L, new BigDecimal("15000.00"), "TRANSFER", 2L, "Fund Transfer from 1001", "NEFT", "SUCCESS");

            // 3. Transfer 3: 1002 to 1001 (reference_id = 3)
            jdbcTemplate.update("""
                INSERT INTO fund_transfers (from_account, to_account, transfer_amount, transfer_mode, created_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '2 days')
                """, 1002L, 1001L, new BigDecimal("25000.00"), "RTGS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '2 days')
                """, 1002L, new BigDecimal("25000.00"), "TRANSFER", 3L, "Fund Transfer to 1001", "RTGS", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '2 days')
                """, 1001L, new BigDecimal("25000.00"), "TRANSFER", 3L, "Fund Transfer from 1002", "RTGS", "SUCCESS");

            // 4. Transfer 4: 1001 to 1002 (reference_id = 4)
            jdbcTemplate.update("""
                INSERT INTO fund_transfers (from_account, to_account, transfer_amount, transfer_mode, created_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '6 hours')
                """, 1001L, 1002L, new BigDecimal("4500.00"), "UPI");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '6 hours')
                """, 1001L, new BigDecimal("4500.00"), "TRANSFER", 4L, "Fund Transfer to 1002", "UPI", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '6 hours')
                """, 1002L, new BigDecimal("4500.00"), "TRANSFER", 4L, "Fund Transfer from 1001", "UPI", "SUCCESS");

            // 5. Transfer 5: 1003 to 1004 (reference_id = 5)
            jdbcTemplate.update("""
                INSERT INTO fund_transfers (from_account, to_account, transfer_amount, transfer_mode, created_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '4 days')
                """, 1003L, 1004L, new BigDecimal("2000.00"), "UPI");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '4 days')
                """, 1003L, new BigDecimal("2000.00"), "TRANSFER", 5L, "Transfer to 1004", "UPI", "SUCCESS");

            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '4 days')
                """, 1004L, new BigDecimal("2000.00"), "TRANSFER", 5L, "Transfer from 1003", "UPI", "SUCCESS");

            log.info("Default rich transaction logs seeded successfully.");
        }
    }
}
