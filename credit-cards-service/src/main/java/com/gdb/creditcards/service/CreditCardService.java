package com.gdb.creditcards.service;

import com.gdb.creditcards.dto.CreditCardDto;
import com.gdb.creditcards.dto.CreditCardTransactionDto;
import java.util.List;

public interface CreditCardService {
    List<CreditCardDto> listUserCards(String userId);
    CreditCardDto applyForCard(CreditCardDto application);
    CreditCardDto getCardDetails(String id);
    CreditCardDto payCreditCardBill(String id, Double amount);
    List<CreditCardTransactionDto> getCardTransactions(String cardId);
}
