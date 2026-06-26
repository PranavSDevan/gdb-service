package com.gdb.creditcards.service.impl;

import com.gdb.creditcards.domain.CreditCard;
import com.gdb.creditcards.domain.CreditCardTransaction;
import com.gdb.creditcards.dto.CreditCardDto;
import com.gdb.creditcards.dto.CreditCardTransactionDto;
import com.gdb.creditcards.repository.CreditCardRepository;
import com.gdb.creditcards.repository.CreditCardTransactionRepository;
import com.gdb.creditcards.service.CreditCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreditCardServiceImpl implements CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final CreditCardTransactionRepository creditCardTransactionRepository;
    private final Random random = new Random();

    @Override
    public List<CreditCardDto> listUserCards(String userId) {
        return creditCardRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CreditCardDto applyForCard(CreditCardDto application) {
        double limit = 100000.0;
        if ("GOLD".equalsIgnoreCase(application.getCardType())) {
            limit = 250000.0;
        } else if ("PLATINUM".equalsIgnoreCase(application.getCardType())) {
            limit = 500000.0;
        }

        CreditCard card = new CreditCard();
        card.setUserId(application.getUserId());
        card.setCardType(application.getCardType().toUpperCase());
        
        // Generate a valid-looking 16-digit card number
        card.setCardNumber("4111" + (100000000000L + (long)(random.nextDouble() * 900000000000L)));
        card.setCreditLimit(limit);
        
        // Seed outstanding balance for testing pay bills options
        double outstanding = "PLATINUM".equalsIgnoreCase(application.getCardType()) ? 100000.0 :
                             "GOLD".equalsIgnoreCase(application.getCardType()) ? 50000.0 : 15000.0;
        card.setOutstandingAmount(outstanding);
        card.setAvailableCredit(limit - outstanding);
        card.setMinimumDue(outstanding * 0.05); // 5% minimum payment due
        card.setNextDueDate(LocalDate.now().plusDays(30));
        card.setCardHolderName(application.getCardHolderName());
        card.setStatus("ACTIVE");

        CreditCard saved = creditCardRepository.save(card);

        // Seed 3 purchase transactions for this new card to populate transaction logs
        seedTransaction(saved.getId(), LocalDateTime.now().minusHours(2), "Amazon", outstanding * 0.70, "Purchase");
        seedTransaction(saved.getId(), LocalDateTime.now().minusHours(5), "Starbucks", outstanding * 0.10, "Purchase");
        seedTransaction(saved.getId(), LocalDateTime.now().minusDays(1), "Uber", outstanding * 0.20, "Purchase");

        return convertToDto(saved);
    }

    private void seedTransaction(String cardId, LocalDateTime date, String merchant, Double amount, String type) {
        CreditCardTransaction transaction = new CreditCardTransaction();
        transaction.setCardId(cardId);
        transaction.setDate(date);
        transaction.setMerchant(merchant);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setStatus("COMPLETED");
        creditCardTransactionRepository.save(transaction);
    }

    @Override
    public CreditCardDto getCardDetails(String id) {
        CreditCard card = creditCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        return convertToDto(card);
    }

    @Override
    public CreditCardDto payCreditCardBill(String id, Double amount) {
        CreditCard card = creditCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        double outstanding = card.getOutstandingAmount() - amount;
        if (outstanding < 0) {
            outstanding = 0.0;
        }
        card.setOutstandingAmount(outstanding);
        card.setAvailableCredit(Math.min(card.getCreditLimit(), card.getAvailableCredit() + amount));
        
        if (outstanding == 0.0) {
            card.setMinimumDue(0.0);
        } else {
            card.setMinimumDue(outstanding * 0.05); // 5% minimum payment due
        }

        CreditCard saved = creditCardRepository.save(card);

        // Record the bill payment transaction in the database
        CreditCardTransaction transaction = new CreditCardTransaction();
        transaction.setCardId(id);
        transaction.setDate(LocalDateTime.now());
        transaction.setMerchant("Credit Card Bill Payment");
        transaction.setAmount(amount);
        transaction.setType("Payment");
        transaction.setStatus("COMPLETED");
        creditCardTransactionRepository.save(transaction);

        return convertToDto(saved);
    }

    private void seedMockTransactions(String cardId) {
        LocalDateTime now = LocalDateTime.now();
        List<Object[]> initialTxns = List.of(
            new Object[]{"Amazon",           4599.00,  "Purchase",  now.minusDays(25)},
            new Object[]{"Uber",              349.00,  "Purchase",  now.minusDays(22)},
            new Object[]{"Flipkart",         8750.00,  "Purchase",  now.minusDays(18)},
            new Object[]{"Swiggy",            620.00,  "Purchase",  now.minusDays(14)},
            new Object[]{"BookMyShow",       1200.00,  "Purchase",  now.minusDays(10)},
            new Object[]{"Netflix",          649.00,   "Purchase",  now.minusDays(7)}
        );
        for (Object[] data : initialTxns) {
            CreditCardTransaction tx = new CreditCardTransaction();
            tx.setCardId(cardId);
            tx.setMerchant((String) data[0]);
            tx.setAmount((Double) data[1]);
            tx.setType((String) data[2]);
            tx.setDate((LocalDateTime) data[3]);
            tx.setStatus("Completed");
            creditCardTransactionRepository.save(tx);
        }
    }

    private CreditCardDto convertToDto(CreditCard card) {
        CreditCardDto dto = new CreditCardDto();
        dto.setId(card.getId());
        dto.setUserId(card.getUserId());
        
        // Format the card number with spaces if it is a 16-digit string
        String rawNumber = card.getCardNumber();
        if (rawNumber != null) {
            String cleanNumber = rawNumber.replaceAll("\\s+", "");
            if (cleanNumber.length() == 16) {
                dto.setCardNumber(cleanNumber.substring(0, 4) + " " + 
                                  cleanNumber.substring(4, 8) + " " + 
                                  cleanNumber.substring(8, 12) + " " + 
                                  cleanNumber.substring(12, 16));
            } else {
                dto.setCardNumber(rawNumber);
            }
        } else {
            dto.setCardNumber(null);
        }
        
        dto.setCardType(card.getCardType());
        dto.setCreditLimit(card.getCreditLimit());
        dto.setAvailableCredit(card.getAvailableCredit());
        dto.setOutstandingAmount(card.getOutstandingAmount());
        dto.setMinimumDue(card.getMinimumDue());
        dto.setNextDueDate(card.getNextDueDate());
        dto.setCardHolderName(card.getCardHolderName());
        dto.setStatus(card.getStatus());
        return dto;
    }

    @Override
    public List<CreditCardTransactionDto> getCardTransactions(String cardId) {
        return creditCardTransactionRepository.findByCardIdOrderByDateDesc(cardId).stream()
                .map(this::convertToTransactionDto)
                .collect(Collectors.toList());
    }

    private CreditCardTransactionDto convertToTransactionDto(CreditCardTransaction txn) {
        CreditCardTransactionDto dto = new CreditCardTransactionDto();
        dto.setId(txn.getId());
        dto.setCardId(txn.getCardId());
        dto.setDate(txn.getDate());
        dto.setMerchant(txn.getMerchant());
        dto.setAmount(txn.getAmount());
        dto.setType(txn.getType());
        dto.setStatus(txn.getStatus());
        return dto;
    }
}
