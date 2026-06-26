package com.gdb.creditcards.config;

import com.gdb.creditcards.domain.CreditCard;
import com.gdb.creditcards.domain.CreditCardTransaction;
import com.gdb.creditcards.repository.CreditCardRepository;
import com.gdb.creditcards.repository.CreditCardTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CreditCardRepository creditCardRepository;
    private final CreditCardTransactionRepository creditCardTransactionRepository;

    @Override
    public void run(String... args) {
        long cardCount = creditCardRepository.count();
        log.info("Current credit card count in DB: {}", cardCount);

        // 1. Ensure user "1" (System Admin) has a card and transactions
        CreditCard adminCard = getOrCreateCard("1", "PLATINUM", "4111222233334444", 500000.0, 125000.0, 375000.0, 18750.0, LocalDate.now().plusDays(5));
        seedTransactionsIfEmpty(adminCard.getId(), true);

        // 2. Ensure user "2" (John Doe) has a card and transactions
        CreditCard johnCard = getOrCreateCard("2", "GOLD", "4111555566667777", 250000.0, 200000.0, 50000.0, 2500.0, LocalDate.now().plusDays(10));
        seedTransactionsIfEmpty(johnCard.getId(), false);

        // 3. Ensure user "3" (Teller User) has a card and transactions
        CreditCard tellerCard = getOrCreateCard("3", "SILVER", "4111888899990000", 100000.0, 85000.0, 15000.0, 750.0, LocalDate.now().plusDays(12));
        seedTransactionsIfEmpty(tellerCard.getId(), false);

        // 4. Ensure user "4" (Manager User) has a card and transactions
        CreditCard managerCard = getOrCreateCard("4", "PREMIUM", "4111111122223333", 300000.0, 260000.0, 40000.0, 2000.0, LocalDate.now().plusDays(15));
        seedTransactionsIfEmpty(managerCard.getId(), false);
    }

    private CreditCard getOrCreateCard(String userId, String cardType, String defaultCardNumber, Double limit, Double available, Double outstanding, Double minDue, LocalDate nextDue) {
        java.util.List<CreditCard> cards = creditCardRepository.findByUserId(userId);
        if (!cards.isEmpty()) {
            CreditCard card = cards.get(0);
            log.info("User {} already has card in DB: ID={}, CardNumber={}", userId, card.getId(), card.getCardNumber());
            return card;
        }

        log.info("User {} has no card. Seeding one...", userId);
        CreditCard card = new CreditCard();
        card.setUserId(userId);
        card.setCardNumber(defaultCardNumber);
        card.setCardType(cardType);
        card.setCreditLimit(limit);
        card.setAvailableCredit(available);
        card.setOutstandingAmount(outstanding);
        card.setMinimumDue(minDue);
        card.setNextDueDate(nextDue);
        card.setStatus("ACTIVE");
        return creditCardRepository.save(card);
    }

    private void seedTransactionsIfEmpty(String cardId, boolean isAdmin) {
        java.util.List<CreditCardTransaction> txns = creditCardTransactionRepository.findByCardIdOrderByDateDesc(cardId);
        if (txns.isEmpty()) {
            log.info("Card {} has 0 transactions in DB. Seeding default transactions...", cardId);
            if (isAdmin) {
                seedTransaction(cardId, LocalDateTime.now().minusDays(1), "Amazon", 125000.0, "Purchase");
                seedTransaction(cardId, LocalDateTime.now().minusDays(2), "Uber", 2500.0, "Purchase");
                seedTransaction(cardId, LocalDateTime.now().minusDays(3), "Flipkart", 247500.0, "Purchase");
                seedTransaction(cardId, LocalDateTime.now().minusDays(4), "Zomato", 1200.0, "Purchase");
                seedTransaction(cardId, LocalDateTime.now().minusDays(6), "Credit Card Bill Payment", 50000.0, "Payment");
            } else {
                seedTransaction(cardId, LocalDateTime.now().minusDays(1), "Amazon", 15000.0, "Purchase");
                seedTransaction(cardId, LocalDateTime.now().minusDays(3), "Swiggy", 3500.0, "Purchase");
                seedTransaction(cardId, LocalDateTime.now().minusDays(5), "Credit Card Bill Payment", 20000.0, "Payment");
            }
        } else {
            log.info("Card {} already has {} transactions in DB.", cardId, txns.size());
        }
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
}
