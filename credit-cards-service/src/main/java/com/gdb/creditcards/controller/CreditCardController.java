package com.gdb.creditcards.controller;

import com.gdb.creditcards.dto.CreditCardDto;
import com.gdb.creditcards.service.CreditCardService;
import com.gdb.creditcards.repository.CreditCardTransactionRepository;
import com.gdb.creditcards.security.SecurityUtils;
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

        SecurityUtils.checkAnyStaffRole();

        if (SecurityUtils.isTeller()) {

            Long currentUserId = SecurityUtils.getCurrentUserId();

            if (currentUserId == null ||
                    !String.valueOf(currentUserId).equals(userId)) {

                throw new RuntimeException("ACCESS_DENIED");
            }
        }

        return ResponseEntity.ok(creditCardService.listUserCards(userId));
    }

    @PostMapping("/apply")
    public ResponseEntity<CreditCardDto> applyForCard(
            @RequestBody CreditCardDto application) {

        SecurityUtils.checkAnyStaffRole();

        if (SecurityUtils.isTeller()) {

            Long currentUserId = SecurityUtils.getCurrentUserId();

            if (currentUserId == null ||
                    !String.valueOf(currentUserId).equals(application.getUserId())) {

                throw new RuntimeException("ACCESS_DENIED");
            }
        }

        return ResponseEntity.ok(
                creditCardService.applyForCard(application));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditCardDto> getCardDetails(
            @PathVariable String id) {

        SecurityUtils.checkAnyStaffRole();

        CreditCardDto card = creditCardService.getCardDetails(id);

        validateTellerCardAccess(card);

        return ResponseEntity.ok(card);
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getCardTransactions(
            @PathVariable String id) {

        SecurityUtils.checkAnyStaffRole();

        CreditCardDto card = creditCardService.getCardDetails(id);

        validateTellerCardAccess(card);

        return ResponseEntity.ok(
                creditCardService.getCardTransactions(id));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payCreditCardBill(
            @PathVariable String id,
            @RequestBody Map<String, Object> payment) {

        SecurityUtils.checkAnyStaffRole();

        CreditCardDto card = creditCardService.getCardDetails(id);

        validateTellerCardAccess(card);

        Double amount =
                Double.valueOf(payment.get("amount").toString());

        return ResponseEntity.ok(
                creditCardService.payCreditCardBill(id, amount));
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<?> makePurchase(
            @PathVariable String id,
            @RequestBody Map<String, Object> purchase) {

        SecurityUtils.checkAnyStaffRole();

        CreditCardDto card = creditCardService.getCardDetails(id);

        validateTellerCardAccess(card);

        String merchant = purchase.get("merchant").toString();
        Double amount =
                Double.valueOf(purchase.get("amount").toString());

        return ResponseEntity.ok(
                creditCardService.makePurchase(id, merchant, amount));
    }

    private void validateTellerCardAccess(CreditCardDto card) {

        if (!SecurityUtils.isTeller()) {
            return;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        if (currentUserId == null ||
                !String.valueOf(currentUserId).equals(card.getUserId())) {

            throw new RuntimeException("ACCESS_DENIED");
        }
    }
}
