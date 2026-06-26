package com.gdb.bankstatements.config;

import com.gdb.bankstatements.domain.StatementTransaction;
import com.gdb.bankstatements.repository.StatementTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StatementTransactionRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            log.info("Seeding default statement transactions into local database...");

            // 1001 (John Doe - Savings)
            saveTx("1", "1001", LocalDate.now().minusDays(10), "Initial Cash Deposit", "CREDIT", "60000.00", "60000.00");
            saveTx("2", "1001", LocalDate.now().minusDays(8), "Fund Transfer from 1002", "CREDIT", "10000.00", "70000.00");
            saveTx("3", "1001", LocalDate.now().minusDays(7), "ATM Cash Withdrawal", "DEBIT", "5000.00", "65000.00");
            saveTx("4", "1001", LocalDate.now().minusDays(6), "Cash Deposit", "CREDIT", "2500.00", "67500.00");
            saveTx("5", "1001", LocalDate.now().minusDays(5), "Fund Transfer to 1002", "DEBIT", "15000.00", "52500.00");
            saveTx("6", "1001", LocalDate.now().minusDays(2), "Fund Transfer from 1002", "CREDIT", "25000.00", "77500.00");
            saveTx("7", "1001", LocalDate.now().minusDays(1), "ATM Cash Deposit", "CREDIT", "8000.00", "85500.00");
            saveTx("8", "1001", LocalDate.now().minusDays(1), "Online Debit Payment", "DEBIT", "1200.00", "84300.00");
            saveTx("9", "1001", LocalDate.now().minusDays(1), "Fund Transfer to 1002", "DEBIT", "4500.00", "79800.00");

            // 1002 (System Admin - Current)
            saveTx("10", "1002", LocalDate.now().minusDays(9), "Initial Cash Deposit", "CREDIT", "150000.00", "150000.00");
            saveTx("11", "1002", LocalDate.now().minusDays(8), "Fund Transfer to 1001", "DEBIT", "10000.00", "140000.00");
            saveTx("12", "1002", LocalDate.now().minusDays(5), "Fund Transfer from 1001", "CREDIT", "15000.00", "155000.00");
            saveTx("13", "1002", LocalDate.now().minusDays(4), "Branch Cash Withdrawal", "DEBIT", "20000.00", "135000.00");
            saveTx("14", "1002", LocalDate.now().minusDays(3), "Online Deposit", "CREDIT", "45000.00", "180000.00");
            saveTx("15", "1002", LocalDate.now().minusDays(2), "Fund Transfer to 1001", "DEBIT", "25000.00", "155000.00");
            saveTx("16", "1002", LocalDate.now().minusDays(1), "Fund Transfer from 1001", "CREDIT", "4500.00", "159500.00");

            // 1003 (Teller User - Savings)
            saveTx("17", "1003", LocalDate.now().minusDays(10), "Salary Credit", "CREDIT", "80000.00", "80000.00");
            saveTx("18", "1003", LocalDate.now().minusDays(5), "ATM Cash Out", "DEBIT", "4500.00", "75500.00");
            saveTx("19", "1003", LocalDate.now().minusDays(4), "Transfer to 1004", "DEBIT", "2000.00", "73500.00");
            saveTx("20", "1003", LocalDate.now().minusDays(2), "Grocery Purchase", "DEBIT", "2000.00", "71500.00");

            // 1004 (Manager User - Current)
            saveTx("21", "1004", LocalDate.now().minusDays(8), "Capital Funding", "CREDIT", "200000.00", "200000.00");
            saveTx("22", "1004", LocalDate.now().minusDays(4), "Transfer from 1003", "CREDIT", "2000.00", "202000.00");
            saveTx("23", "1004", LocalDate.now().minusDays(3), "Office Supplies", "DEBIT", "15000.00", "187000.00");
            saveTx("24", "1004", LocalDate.now().minusDays(1), "Client Fee Payment", "CREDIT", "8000.00", "195000.00");

            log.info("Completed seeding statement transactions.");
        }
    }

    private void saveTx(String id, String accountId, LocalDate date, String desc, String type, String amount, String balance) {
        StatementTransaction tx = new StatementTransaction();
        tx.setId(id);
        tx.setAccountId(accountId);
        tx.setTransactionDate(date);
        tx.setDescription(desc);
        tx.setTransactionType(type);
        tx.setAmount(new BigDecimal(amount));
        tx.setBalance(new BigDecimal(balance));
        repository.save(tx);
    }
}
