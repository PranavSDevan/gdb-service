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
        log.info("Clearing existing credit card database records for clean seeding...");
        creditCardTransactionRepository.deleteAll();
        creditCardRepository.deleteAll();

        // 1. System Admin (Priya Sharma) - User 1
        CreditCard adminCard = getOrCreateCard("1", "PLATINUM", "4111222233334444", 500000.0, 125000.0, 375000.0, 18750.0, LocalDate.now().plusDays(5), "Priya Sharma");
        seedTransactionsIfEmpty(adminCard.getId(), true);

        // 2. John Doe - User 7
        CreditCard johnCard = getOrCreateCard("7", "GOLD", "4111555566667777", 250000.0, 200000.0, 50000.0, 2500.0, LocalDate.now().plusDays(10), "John Doe");
        seedTransactionsIfEmpty(johnCard.getId(), false);

        // 3. Teller User - User 8
        CreditCard tellerCard = getOrCreateCard("8", "SILVER", "4111888899990000", 100000.0, 85000.0, 15000.0, 750.0, LocalDate.now().plusDays(12), "Teller User");
        seedTransactionsIfEmpty(tellerCard.getId(), false);

        // 4. Manager User - User 9
        CreditCard managerCard = getOrCreateCard("9", "PLATINUM", "4111111122223333", 300000.0, 260000.0, 40000.0, 2000.0, LocalDate.now().plusDays(15), "Manager User");
        seedTransactionsIfEmpty(managerCard.getId(), false);

        // 5. Manager Rajesh Kumar - User 2
        CreditCard rajeshCard = getOrCreateCard("2", "GOLD", "4111222244448888", 250000.0, 250000.0, 0.0, 0.0, LocalDate.now().plusDays(20), "Rajesh Kumar");
        seedTransactionsIfEmpty(rajeshCard.getId(), false);

        // 6. Manager Sunita Agarwal - User 3
        CreditCard sunitaCard = getOrCreateCard("3", "GOLD", "4111333366669999", 250000.0, 250000.0, 0.0, 0.0, LocalDate.now().plusDays(20), "Sunita Agarwal");
        seedTransactionsIfEmpty(sunitaCard.getId(), false);

        // 7. Teller Amit Verma - User 4
        CreditCard amitCard = getOrCreateCard("4", "SILVER", "4111444488881111", 100000.0, 100000.0, 0.0, 0.0, LocalDate.now().plusDays(20), "Amit Verma");
        seedTransactionsIfEmpty(amitCard.getId(), false);

        // 8. Teller Neha Singh - User 5
        CreditCard nehaCard = getOrCreateCard("5", "SILVER", "4111555599992222", 100000.0, 100000.0, 0.0, 0.0, LocalDate.now().plusDays(20), "Neha Singh");
        seedTransactionsIfEmpty(nehaCard.getId(), false);
    }

    private CreditCard getOrCreateCard(String userId, String cardType, String defaultCardNumber, Double limit, Double available, Double outstanding, Double minDue, LocalDate nextDue, String cardHolderName) {
        log.info("Seeding card for user {} ({})", userId, cardHolderName);
        CreditCard card = new CreditCard();
        card.setUserId(userId);
        card.setCardNumber(defaultCardNumber);
        card.setCardType(cardType);
        card.setCreditLimit(limit);
        card.setAvailableCredit(available);
        card.setOutstandingAmount(outstanding);
        card.setMinimumDue(minDue);
        card.setNextDueDate(nextDue);
        card.setCardHolderName(cardHolderName);
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
