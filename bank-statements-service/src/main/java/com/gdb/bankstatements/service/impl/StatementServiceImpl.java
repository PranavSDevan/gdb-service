package com.gdb.bankstatements.service.impl;

import com.gdb.bankstatements.domain.Statement;
import com.gdb.bankstatements.dto.StatementDto;
import com.gdb.bankstatements.repository.StatementRepository;
import com.gdb.bankstatements.service.StatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final StatementRepository statementRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${transactions.service.url}")
    private String transactionsServiceUrl;

    @Override
    public StatementDto generateStatement(StatementDto request) {
        Statement statement = new Statement();
        statement.setAccountId(request.getAccountId());
        statement.setFromDate(request.getFromDate());
        statement.setToDate(request.getToDate());
        statement.setFormat(request.getFormat());
        statement.setStatus("COMPLETED");

        Statement saved = statementRepository.save(statement);
        saved.setDownloadUrl("/api/v1/statements/" + saved.getId() + "/download");
        saved = statementRepository.save(saved);

        return convertToDto(saved);
    }

    @Override
    public StatementDto getStatementStatus(String id) {
        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Statement not found"));
        return convertToDto(statement);
    }

    @Override
    public byte[] downloadStatement(String id) {
        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Statement not found"));

        // Fetch transactions from transactions-service if available, else generate fallback data
        String transactionsCsv = getTransactionsCsv(statement.getAccountId(), statement.getFromDate(), statement.getToDate());

        String content = "GDB BANK STATEMENT\n" +
                "====================================\n" +
                "Statement ID: " + statement.getId() + "\n" +
                "Account ID / Number: " + statement.getAccountId() + "\n" +
                "Statement Period: " + statement.getFromDate() + " to " + statement.getToDate() + "\n" +
                "Format: " + statement.getFormat() + "\n" +
                "Generated Date: " + LocalDate.now() + "\n" +
                "====================================\n\n" +
                "DATE,DESCRIPTION,TYPE,AMOUNT,BALANCE\n" +
                transactionsCsv +
                "\n====================================\n" +
                "End of Statement\n";

        return content.getBytes(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    private String getTransactionsCsv(String accountId, LocalDate fromDate, LocalDate toDate) {
        StringBuilder csv = new StringBuilder();
        try {
            String url = transactionsServiceUrl + "/api/v1/transaction-logs/" + accountId;
            log.info("Attempting to fetch transaction logs from URL: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            // Propagate the Authorization header from the incoming request to transactions-service
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String authHeader = attributes.getRequest().getHeader("Authorization");
                if (authHeader != null) {
                    headers.set("Authorization", authHeader);
                }
            }

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.containsKey("logs")) {
                List<Map<String, Object>> logs = (List<Map<String, Object>>) response.get("logs");
                for (Map<String, Object> logItem : logs) {
                    String date = String.valueOf(logItem.get("created_at"));
                    String desc = String.valueOf(logItem.get("description"));
                    String type = String.valueOf(logItem.get("transaction_type"));
                    String amount = String.valueOf(logItem.get("amount"));
                    if (date != null && !date.equals("null") && date.length() >= 10) {
                        // Try to filter by date range
                        LocalDate logDate = LocalDate.parse(date.substring(0, 10));
                        if ((logDate.isAfter(fromDate) || logDate.isEqual(fromDate)) &&
                                (logDate.isBefore(toDate) || logDate.isEqual(toDate))) {
                            csv.append(date.substring(0, 10)).append(",")
                               .append(desc).append(",")
                               .append(type).append(",")
                               .append(amount).append(",N/A\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch transactions from transaction-service: {}", e.getMessage(), e);
        }

        if (csv.length() == 0) {
            csv.append("No transaction history found for the specified period.\n");
        }

        return csv.toString();
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
