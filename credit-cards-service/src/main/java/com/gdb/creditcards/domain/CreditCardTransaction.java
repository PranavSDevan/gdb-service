package com.gdb.creditcards.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_card_transactions")
@Data
public class CreditCardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "card_id")
    private String cardId;

    private String merchant;

    private Double amount;

    private String type; // Purchase, Payment, Refund

    private LocalDateTime date;

    private String status; // Completed, Pending
}
