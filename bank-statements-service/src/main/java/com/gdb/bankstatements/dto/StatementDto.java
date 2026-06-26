package com.gdb.bankstatements.dto;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Data
public class StatementDto {
    private String id;
    private String accountId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String format;
    private String status;
    private String downloadUrl;
    private List<StatementTransactionDto> transactions;
    private StatementSummaryDto summary;

    @Data
    public static class StatementTransactionDto {
        private String id;
        private LocalDate transactionDate;
        private String description;
        private String transactionType;
        private BigDecimal amount;
        private BigDecimal balance;
    }

    @Data
    public static class StatementSummaryDto {
        private BigDecimal openingBalance;
        private BigDecimal closingBalance;
        private BigDecimal totalCredits;
        private BigDecimal totalDebits;
        private Integer transactionCount;
    }
}
