package com.gdb.creditcards.controller;

import com.gdb.creditcards.dto.CreditCardDto;
import com.gdb.creditcards.service.CreditCardService;
import com.gdb.creditcards.repository.CreditCardTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/credit-cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService creditCardService;
    private final CreditCardTransactionRepository creditCardTransactionRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CreditCardDto>> listUserCards(@PathVariable String userId) {
        return ResponseEntity.ok(creditCardService.listUserCards(userId));
    }

    @PostMapping("/apply")
    public ResponseEntity<CreditCardDto> applyForCard(@RequestBody CreditCardDto application) {
        return ResponseEntity.ok(creditCardService.applyForCard(application));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditCardDto> getCardDetails(@PathVariable String id) {
        return ResponseEntity.ok(creditCardService.getCardDetails(id));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getCardTransactions(@PathVariable String id) {
        return ResponseEntity.ok(creditCardService.getCardTransactions(id));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payCreditCardBill(@PathVariable String id, @RequestBody Map<String, Object> payment) {
        Double amount = Double.valueOf(payment.get("amount").toString());
        return ResponseEntity.ok(creditCardService.payCreditCardBill(id, amount));
    }
}
