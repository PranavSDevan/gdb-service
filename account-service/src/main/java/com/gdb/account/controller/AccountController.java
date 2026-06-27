package com.gdb.account.controller;

import com.gdb.account.dto.response.AccountResponse;
import com.gdb.account.dto.request.SavingsAccountRequest;
import com.gdb.account.dto.request.CurrentAccountRequest;
import com.gdb.account.service.AccountService;
import com.gdb.account.constants.AccountConstants;
import com.gdb.account.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public REST Controller for Account operations.
 */
@RestController
@RequestMapping(AccountConstants.API_V1)
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final com.gdb.account.client.UserClient userClient;

    @PostMapping("/savings")
    public ResponseEntity<AccountResponse> createSavingsAccount(@Valid @RequestBody SavingsAccountRequest request) {
        SecurityUtils.checkAnyStaffRole(); // Everyone (Admin, Manager, Teller) can create accounts
        checkTellerKycRestriction(request.getAadharNumber());
        AccountResponse account = accountService.createSavingsAccount(request);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }

    @PostMapping("/current")
    public ResponseEntity<AccountResponse> createCurrentAccount(@Valid @RequestBody CurrentAccountRequest request) {
        SecurityUtils.checkAnyStaffRole(); // Everyone (Admin, Manager, Teller) can create accounts
        checkTellerKycRestriction(request.getRegistrationNo());
        AccountResponse account = accountService.createCurrentAccount(request);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long accountNumber) {
        SecurityUtils.checkAnyStaffRole();
        AccountResponse response = accountService.getAccountByNumber(accountNumber);
        checkTellerAccess(response);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String privilege,
            @RequestParam(required = false) Boolean active) {
        SecurityUtils.checkAnyStaffRole();
        return ResponseEntity.ok(accountService.getAllAccounts(type, privilege, active));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<java.util.Map<String, Object>> getBalance(@PathVariable Long accountNumber) {
        SecurityUtils.checkAnyStaffRole();
        AccountResponse account = accountService.getAccountByNumber(accountNumber);
        checkTellerAccess(account);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("account_number", account.getAccountNumber());
        response.put("balance", account.getBalance());
        response.put("currency", AccountConstants.CURRENCY_INR);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountNumber}/verify-pin")
    public ResponseEntity<java.util.Map<String, Boolean>> verifyPin(
            @PathVariable Long accountNumber,
            @RequestBody java.util.Map<String, String> request) {
        SecurityUtils.checkAnyStaffRole();
        AccountResponse account = accountService.getAccountByNumber(accountNumber);
        checkTellerAccess(account);
        String pin = request.get("pin");
        boolean isValid = accountService.verifyPin(accountNumber, pin);
        return ResponseEntity.ok(java.util.Map.of("valid", isValid));
    }

    private void checkTellerKycRestriction(String requestKyc) {
        com.gdb.account.security.UserContext context = com.gdb.account.security.UserContextHolder.getContext();
        if (context != null && context.getRole() != null) {
            String role = context.getRole().toUpperCase().trim();
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            if ("TELLER".equals(role)) {
                String tellerKyc = userClient.getKycNumber(context.getLoginId());
                if (tellerKyc == null || !tellerKyc.equals(requestKyc)) {
                    throw new RuntimeException("Access Denied: Tellers can only create accounts using their own KYC number.");
                }
            }
        }
    }

    private void checkTellerAccess(AccountResponse account) {
        com.gdb.account.security.UserContext context = com.gdb.account.security.UserContextHolder.getContext();
        if (context != null && context.getRole() != null) {
            String role = context.getRole().toUpperCase().trim();
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            if ("TELLER".equals(role)) {
                String tellerName = userClient.getUsername(context.getLoginId());
                if (tellerName == null || account.getName() == null || !tellerName.equalsIgnoreCase(account.getName())) {
                    throw new RuntimeException("ACCESS_DENIED");
                }
            }
        }
    }
}
