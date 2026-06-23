package com.gdb.transactions.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gdb.transactions.domain.enums.TransferMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for fund transfer operations with snake_case json mappings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferResponse {
    private String status;

    @JsonProperty("transaction_id")
    private Long transactionId;

    @JsonProperty("from_account")
    private Long fromAccount;

    @JsonProperty("to_account")
    private Long toAccount;

    private BigDecimal amount;

    @JsonProperty("transfer_mode")
    private TransferMode transferMode;

    private String description;

    @JsonProperty("from_account_new_balance")
    private BigDecimal fromAccountNewBalance;

    @JsonProperty("to_account_new_balance")
    private BigDecimal toAccountNewBalance;

    @JsonProperty("transaction_date")
    private LocalDateTime transactionDate;
}