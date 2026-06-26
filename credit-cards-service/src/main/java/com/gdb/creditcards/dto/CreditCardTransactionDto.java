package com.gdb.creditcards.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreditCardTransactionDto {
    private String id;
    private String cardId;
    private LocalDateTime date;
    private String merchant;
    private Double amount;
    private String type; // Purchase, Payment, Refund
    private String status; // Completed, Pending
}
