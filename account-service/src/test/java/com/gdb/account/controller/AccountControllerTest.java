package com.gdb.account.controller;

import com.gdb.account.service.AccountService;
import com.gdb.account.exception.AccountException;
import com.gdb.account.constants.AccountConstants;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; // <-- Add this import
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

// TODO: MOD9-CR-01: MockMvc Integration Tests.
// Trainee task: Write integration tests for AccountController using @WebMvcTest.
// Write tests for GET /api/v1/accounts/{accountNumber} and POST /api/v1/accounts/savings.
// Verify status codes (200, 201, 400, 422) and response body payloads.
@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    public void testGetAccountByNumber_NotFound() throws Exception {
        // Trainee: Write a test case here that mocks the service and asserts 404 response
        Long fakeAccountNumber = 99999999L;

        // Mocking service layer to throw an AccountException simulating Account Not Found
        Mockito.when(accountService.getAccountByNumber(fakeAccountNumber))
                .thenThrow(new AccountException("Account not found", AccountConstants.ACCOUNT_NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/accounts/" + fakeAccountNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound()); // Asserts HTTP 404 Status Code
    }
}
