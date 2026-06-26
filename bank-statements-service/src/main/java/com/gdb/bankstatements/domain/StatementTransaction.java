package com.gdb.bankstatements.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "statement_transactions")
@Data
public class StatementTransaction {
    @Id
    private String id;
    
    @Column(name = "account_id")
    private String accountId;
    
    @Column(name = "transaction_date")
    private LocalDate transactionDate;
    
    private String description;
    
    @Column(name = "transaction_type")
    private String transactionType;
    
    private BigDecimal amount;
    
    private BigDecimal balance;
}
