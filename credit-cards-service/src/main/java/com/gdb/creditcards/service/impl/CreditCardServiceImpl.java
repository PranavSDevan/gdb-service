package com.gdb.creditcards.service.impl;

import com.gdb.creditcards.domain.CreditCard;
import com.gdb.creditcards.dto.CreditCardDto;
import com.gdb.creditcards.repository.CreditCardRepository;
import com.gdb.creditcards.service.CreditCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreditCardServiceImpl implements CreditCardService {

    private final CreditCardRepository creditCardRepository;
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
        card.setAvailableCredit(limit);
        card.setOutstandingAmount(0.0);
        card.setMinimumDue(0.0);
        card.setNextDueDate(LocalDate.now().plusDays(30));
        card.setStatus("ACTIVE");

        CreditCard saved = creditCardRepository.save(card);
        return convertToDto(saved);
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
        return convertToDto(saved);
    }

    private CreditCardDto convertToDto(CreditCard card) {
        CreditCardDto dto = new CreditCardDto();
        dto.setId(card.getId());
        dto.setUserId(card.getUserId());
        dto.setCardNumber(card.getCardNumber());
        dto.setCardType(card.getCardType());
        dto.setCreditLimit(card.getCreditLimit());
        dto.setAvailableCredit(card.getAvailableCredit());
        dto.setOutstandingAmount(card.getOutstandingAmount());
        dto.setMinimumDue(card.getMinimumDue());
        dto.setNextDueDate(card.getNextDueDate());
        dto.setStatus(card.getStatus());
        return dto;
    }
}
