package com.gdb.creditcards.controller;

import com.gdb.creditcards.dto.CreditCardDto;
import com.gdb.creditcards.service.CreditCardService;
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
        // Return a static list of mock transactions since there is no CreditCardTransaction table.
        return ResponseEntity.ok(List.of(
                Map.of("id", "TXN1001", "merchant", "Amazon", "amount", 125000.0, "type", "Purchase", "date", "2026-06-25T10:00:00Z", "status", "Completed"),
                Map.of("id", "TXN1002", "merchant", "Uber", "amount", 2500.0, "type", "Purchase", "date", "2026-06-24T18:30:00Z", "status", "Completed"),
                Map.of("id", "TXN1003", "merchant", "Flipkart", "amount", 247500.0, "type", "Purchase", "date", "2026-06-23T14:45:00Z", "status", "Completed")
        ));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payCreditCardBill(@PathVariable String id, @RequestBody Map<String, Object> payment) {
        Double amount = Double.valueOf(payment.get("amount").toString());
        return ResponseEntity.ok(creditCardService.payCreditCardBill(id, amount));
    }
}
