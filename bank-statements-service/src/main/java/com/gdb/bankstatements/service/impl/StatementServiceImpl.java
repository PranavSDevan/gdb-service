package com.gdb.bankstatements.service.impl;

import com.gdb.bankstatements.domain.Statement;
import com.gdb.bankstatements.domain.StatementTransaction;
import com.gdb.bankstatements.dto.StatementDto;
import com.gdb.bankstatements.repository.StatementRepository;
import com.gdb.bankstatements.repository.StatementTransactionRepository;
import com.gdb.bankstatements.service.StatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final StatementRepository statementRepository;
    private final StatementTransactionRepository statementTransactionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.services.transactions-url:http://localhost:8002}")
    private String transactionsServiceUrl;

    @Value("${app.services.accounts-url:http://localhost:8001}")
    private String accountsServiceUrl;

    @Override
    public StatementDto generateStatement(StatementDto request) {
        // Retrieve current HTTP request and auth header from incoming request thread for propagation
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String authHeader = null;
        if (attributes != null) {
            HttpServletRequest currentRequest = attributes.getRequest();
            authHeader = currentRequest.getHeader("Authorization");
        }

        // 1. Sync latest transactions from transactions-service to local database
        syncTransactions(request.getAccountId(), authHeader);

        // 2. Fetch current balance from account-service
        BigDecimal currentBalance = getAccountBalance(request.getAccountId(), authHeader);

        // 3. Retrieve all transactions from local database for balance computation
        List<StatementTransaction> allTxs = statementTransactionRepository.findByAccountIdOrderByTransactionDateDesc(request.getAccountId());
        
        // Compute running balances backwards starting from the current balance (assuming the list is sorted desc, newest first)
        computeRunningBalances(allTxs, currentBalance);

        // 4. Apply Date Filter
        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();
        List<StatementTransaction> filteredTxs = allTxs.stream()
                .filter(tx -> {
                    LocalDate txDate = tx.getTransactionDate();
                    return (fromDate == null || !txDate.isBefore(fromDate)) &&
                           (toDate == null || !txDate.isAfter(toDate));
                })
                .collect(Collectors.toList());

        // Calculate summary
        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalDebits = BigDecimal.ZERO;
        for (StatementTransaction tx : filteredTxs) {
            if ("CREDIT".equalsIgnoreCase(tx.getTransactionType())) {
                totalCredits = totalCredits.add(tx.getAmount());
            } else {
                totalDebits = totalDebits.add(tx.getAmount());
            }
        }

        BigDecimal openingBalance = currentBalance;
        BigDecimal closingBalance = currentBalance;

        if (!filteredTxs.isEmpty()) {
            // Newest filtered transaction is at index 0 (descending order)
            closingBalance = filteredTxs.get(0).getBalance();
            
            // Oldest filtered transaction is at the last index
            StatementTransaction oldestTx = filteredTxs.get(filteredTxs.size() - 1);
            if ("CREDIT".equalsIgnoreCase(oldestTx.getTransactionType())) {
                openingBalance = oldestTx.getBalance().subtract(oldestTx.getAmount());
            } else {
                openingBalance = oldestTx.getBalance().add(oldestTx.getAmount());
            }
        }

        // 5. Save the generated statement entry
        Statement statement = new Statement();
        statement.setAccountId(request.getAccountId());
        statement.setFromDate(fromDate);
        statement.setToDate(toDate);
        statement.setFormat(request.getFormat());
        statement.setStatus("COMPLETED");

        Statement saved = statementRepository.save(statement);
        saved.setDownloadUrl("/api/v1/statements/" + saved.getId() + "/download");
        saved = statementRepository.save(saved);

        StatementDto responseDto = convertToDto(saved);
        
        // Map transactions to DTO
        List<StatementDto.StatementTransactionDto> txDtos = filteredTxs.stream()
                .map(tx -> {
                    StatementDto.StatementTransactionDto dto = new StatementDto.StatementTransactionDto();
                    dto.setId(tx.getId());
                    dto.setTransactionDate(tx.getTransactionDate());
                    dto.setDescription(tx.getDescription());
                    dto.setTransactionType(tx.getTransactionType());
                    dto.setAmount(tx.getAmount());
                    dto.setBalance(tx.getBalance());
                    return dto;
                })
                .collect(Collectors.toList());
        responseDto.setTransactions(txDtos);

        // Map summary to DTO
        StatementDto.StatementSummaryDto summaryDto = new StatementDto.StatementSummaryDto();
        summaryDto.setOpeningBalance(openingBalance);
        summaryDto.setClosingBalance(closingBalance);
        summaryDto.setTotalCredits(totalCredits);
        summaryDto.setTotalDebits(totalDebits);
        summaryDto.setTransactionCount(filteredTxs.size());
        responseDto.setSummary(summaryDto);

        return responseDto;
    }

    @Override
    public StatementDto getStatementStatus(String id) {
        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Statement not found"));
        
        StatementDto responseDto = convertToDto(statement);

        // Populate details for this statement by re-calculating from the database
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String authHeader = null;
        if (attributes != null) {
            HttpServletRequest currentRequest = attributes.getRequest();
            authHeader = currentRequest.getHeader("Authorization");
        }

        BigDecimal currentBalance = getAccountBalance(statement.getAccountId(), authHeader);
        List<StatementTransaction> allTxs = statementTransactionRepository.findByAccountIdOrderByTransactionDateDesc(statement.getAccountId());
        computeRunningBalances(allTxs, currentBalance);

        LocalDate fromDate = statement.getFromDate();
        LocalDate toDate = statement.getToDate();
        List<StatementTransaction> filteredTxs = allTxs.stream()
                .filter(tx -> {
                    LocalDate txDate = tx.getTransactionDate();
                    return (fromDate == null || !txDate.isBefore(fromDate)) &&
                           (toDate == null || !txDate.isAfter(toDate));
                })
                .collect(Collectors.toList());

        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalDebits = BigDecimal.ZERO;
        for (StatementTransaction tx : filteredTxs) {
            if ("CREDIT".equalsIgnoreCase(tx.getTransactionType())) {
                totalCredits = totalCredits.add(tx.getAmount());
            } else {
                totalDebits = totalDebits.add(tx.getAmount());
            }
        }

        BigDecimal openingBalance = currentBalance;
        BigDecimal closingBalance = currentBalance;

        if (!filteredTxs.isEmpty()) {
            closingBalance = filteredTxs.get(0).getBalance();
            StatementTransaction oldestTx = filteredTxs.get(filteredTxs.size() - 1);
            if ("CREDIT".equalsIgnoreCase(oldestTx.getTransactionType())) {
                openingBalance = oldestTx.getBalance().subtract(oldestTx.getAmount());
            } else {
                openingBalance = oldestTx.getBalance().add(oldestTx.getAmount());
            }
        }

        List<StatementDto.StatementTransactionDto> txDtos = filteredTxs.stream()
                .map(tx -> {
                    StatementDto.StatementTransactionDto dto = new StatementDto.StatementTransactionDto();
                    dto.setId(tx.getId());
                    dto.setTransactionDate(tx.getTransactionDate());
                    dto.setDescription(tx.getDescription());
                    dto.setTransactionType(tx.getTransactionType());
                    dto.setAmount(tx.getAmount());
                    dto.setBalance(tx.getBalance());
                    return dto;
                })
                .collect(Collectors.toList());
        responseDto.setTransactions(txDtos);

        StatementDto.StatementSummaryDto summaryDto = new StatementDto.StatementSummaryDto();
        summaryDto.setOpeningBalance(openingBalance);
        summaryDto.setClosingBalance(closingBalance);
        summaryDto.setTotalCredits(totalCredits);
        summaryDto.setTotalDebits(totalDebits);
        summaryDto.setTransactionCount(filteredTxs.size());
        responseDto.setSummary(summaryDto);

        return responseDto;
    }

    @Override
    public byte[] downloadStatement(String id) {
        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Statement not found"));

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String authHeader = null;
        if (attributes != null) {
            HttpServletRequest currentRequest = attributes.getRequest();
            authHeader = currentRequest.getHeader("Authorization");
        }

        BigDecimal currentBalance = getAccountBalance(statement.getAccountId(), authHeader);
        List<StatementTransaction> allTxs = statementTransactionRepository.findByAccountIdOrderByTransactionDateDesc(statement.getAccountId());
        computeRunningBalances(allTxs, currentBalance);

        LocalDate fromDate = statement.getFromDate();
        LocalDate toDate = statement.getToDate();
        List<StatementTransaction> filteredTxs = allTxs.stream()
                .filter(tx -> {
                    LocalDate txDate = tx.getTransactionDate();
                    return (fromDate == null || !txDate.isBefore(fromDate)) &&
                           (toDate == null || !txDate.isAfter(toDate));
                })
                .collect(Collectors.toList());

        // Build CSV representation
        StringBuilder csv = new StringBuilder();
        for (StatementTransaction tx : filteredTxs) {
            csv.append(tx.getTransactionDate()).append(",")
               .append(tx.getDescription()).append(",")
               .append(tx.getTransactionType()).append(",")
               .append(tx.getAmount()).append(",")
               .append(tx.getBalance()).append("\n");
        }

        String content = "GDB BANK STATEMENT\n" +
                "====================================\n" +
                "Statement ID: " + statement.getId() + "\n" +
                "Account ID / Number: " + statement.getAccountId() + "\n" +
                "Statement Period: " + statement.getFromDate() + " to " + statement.getToDate() + "\n" +
                "Format: " + statement.getFormat() + "\n" +
                "Generated Date: " + LocalDate.now() + "\n" +
                "====================================\n\n" +
                "DATE,DESCRIPTION,TYPE,AMOUNT,BALANCE\n" +
                csv.toString() +
                "\n====================================\n" +
                "End of Statement\n";

        return content.getBytes(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    private void syncTransactions(String accountId, String authHeader) {
        try {
            String url = transactionsServiceUrl + "/api/v1/transaction-logs/" + accountId + "?limit=1000";
            HttpHeaders headers = new HttpHeaders();
            if (authHeader != null) {
                headers.set("Authorization", authHeader);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.containsKey("logs")) {
                List<Map<String, Object>> logs = (List<Map<String, Object>>) response.get("logs");
                for (Map<String, Object> logItem : logs) {
                    String id = String.valueOf(logItem.get("id"));

                    if (statementTransactionRepository.existsById(id)) {
                        continue;
                    }

                    String dateStr = String.valueOf(logItem.get("created_at") != null ? logItem.get("created_at") : logItem.get("transactionDate"));
                    LocalDate logDate = parseDate(dateStr);
                    String desc = String.valueOf(logItem.get("description"));
                    String rawType = String.valueOf(logItem.get("transaction_type") != null ? logItem.get("transaction_type") : logItem.get("transactionType"));

                    String type = "DEBIT";
                    if ("DEPOSIT".equalsIgnoreCase(rawType) || "CREDIT".equalsIgnoreCase(rawType)) {
                        type = "CREDIT";
                    } else if ("TRANSFER".equalsIgnoreCase(rawType)) {
                        Object toAccountObj = logItem.get("to_account");
                        if (toAccountObj != null && String.valueOf(toAccountObj).equals(accountId)) {
                            type = "CREDIT";
                        } else {
                            type = "DEBIT";
                        }
                    }

                    BigDecimal amount = new BigDecimal(String.valueOf(logItem.get("amount")));

                    StatementTransaction tx = new StatementTransaction();
                    tx.setId(id);
                    tx.setAccountId(accountId);
                    tx.setTransactionDate(logDate);
                    tx.setDescription(desc);
                    tx.setTransactionType(type);
                    tx.setAmount(amount);
                    tx.setBalance(BigDecimal.ZERO); // compute balance dynamically

                    statementTransactionRepository.save(tx);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to sync transactions from transactions-service for account {}: {}", accountId, e.getMessage());
        }
    }

    private void computeRunningBalances(List<StatementTransaction> txs, BigDecimal currentBalance) {
        BigDecimal balance = currentBalance;
        for (int i = 0; i < txs.size(); i++) {
            StatementTransaction tx = txs.get(i);
            tx.setBalance(balance);
            if ("CREDIT".equalsIgnoreCase(tx.getTransactionType())) {
                balance = balance.subtract(tx.getAmount());
            } else {
                balance = balance.add(tx.getAmount());
            }
        }
    }

    private BigDecimal getAccountBalance(String accountId, String authHeader) {
        try {
            String url = accountsServiceUrl + "/api/v1/accounts/" + accountId + "/balance";
            HttpHeaders headers = new HttpHeaders();
            if (authHeader != null) {
                headers.set("Authorization", authHeader);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> response = responseEntity.getBody();
            if (response != null && response.containsKey("balance")) {
                return new BigDecimal(String.valueOf(response.get("balance")));
            }
        } catch (Exception e) {
            log.warn("Failed to fetch balance from account-service for account {}: {}", accountId, e.getMessage());
        }
        return new BigDecimal("50000.00"); // fallback
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || "null".equalsIgnoreCase(dateStr)) {
            return LocalDate.now();
        }
        if (dateStr.length() >= 10) {
            try {
                return LocalDate.parse(dateStr.substring(0, 10));
            } catch (Exception e) {
                log.warn("Failed to parse date: {}", dateStr, e);
            }
        }
        return LocalDate.now();
    }

    private StatementDto convertToDto(Statement statement) {
        StatementDto dto = new StatementDto();
        dto.setId(statement.getId());
        dto.setAccountId(statement.getAccountId());
        dto.setFromDate(statement.getFromDate());
        dto.setToDate(statement.getToDate());
        dto.setFormat(statement.getFormat());
        dto.setStatus(statement.getStatus());
        dto.setDownloadUrl(statement.getDownloadUrl());
        return dto;
    }
}
