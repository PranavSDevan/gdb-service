package com.gdb.account.service.impl;

import com.gdb.account.client.AadharClient;
import com.gdb.account.client.CompanyClient;
import com.gdb.account.client.UserClient;
import com.gdb.account.dto.request.SavingsAccountRequest;
import com.gdb.account.dto.response.AccountResponse;
import com.gdb.account.exception.AccountException;
import com.gdb.account.repository.AccountRepository;
import com.gdb.account.security.UserContext;
import com.gdb.account.security.UserContextHolder;
import com.gdb.account.domain.model.Account;
import com.gdb.account.factory.AccountFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountFactory accountFactory;

    @Mock
    private AadharClient aadharClient;

    @Mock
    private CompanyClient companyClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private AccountServiceImpl accountService;

    private SavingsAccountRequest savingsRequest;
    private Map<String, Object> mockUser;

    @BeforeEach
    void setUp() {
        UserContextHolder.clearContext();

        savingsRequest = new SavingsAccountRequest();
        savingsRequest.setName("Amit Verma");
        savingsRequest.setAadharNumber("696673394911");
        savingsRequest.setPin("1357");
        savingsRequest.setDateOfBirth("1990-05-12");
        savingsRequest.setGender("Male");
        savingsRequest.setPhoneNo("9876543210");
        savingsRequest.setPrivilege("GOLD");
        savingsRequest.setInitialBalance(java.math.BigDecimal.valueOf(2500.0));

        mockUser = new HashMap<>();
        mockUser.put("user_id", 9L);
        mockUser.put("login_id", "teller1");
        mockUser.put("username", "Amit Verma");
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clearContext();
    }

    @Test
    void testCreateSavingsAccount_KycNotFound() {
        Mockito.when(userClient.getUserByKyc("696673394911")).thenReturn(null);

        AccountException ex = assertThrows(AccountException.class, () ->
                accountService.createSavingsAccount(savingsRequest));
        assertEquals("KYC_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void testCreateSavingsAccount_TellerMismatch() {
        // Logged in as teller1 (name: Amit Verma)
        UserContext context = UserContext.builder()
                .userId(9L)
                .loginId("teller1")
                .role("TELLER")
                .build();
        UserContextHolder.setContext(context);

        // Lookup KYC number belongs to Priya Sharma
        Map<String, Object> priyaUser = new HashMap<>();
        priyaUser.put("username", "Priya Sharma");

        Mockito.when(userClient.getUserByKyc("696673394911")).thenReturn(priyaUser);
        Mockito.when(userClient.getUsername("teller1")).thenReturn("Amit Verma");

        AccountException ex = assertThrows(AccountException.class, () ->
                accountService.createSavingsAccount(savingsRequest));
        assertEquals("ACCESS_DENIED", ex.getErrorCode());
    }

    @Test
    void testCreateSavingsAccount_TellerMatch_Success() {
        UserContext context = UserContext.builder()
                .userId(9L)
                .loginId("teller1")
                .role("TELLER")
                .build();
        UserContextHolder.setContext(context);

        Mockito.when(userClient.getUserByKyc("696673394911")).thenReturn(mockUser);
        Mockito.when(userClient.getUsername("teller1")).thenReturn("Amit Verma");
        Mockito.when(aadharClient.verifyAadhar("696673394911")).thenReturn(true);
        Mockito.when(accountRepository.existsByNameAndDob("Amit Verma", "1990-05-12")).thenReturn(false);
        Mockito.when(accountRepository.generateAccountNumber()).thenReturn(1001L);

        Account mockAccount = Account.builder()
                .accountNumber(1001L)
                .name("Amit Verma")
                .balance(java.math.BigDecimal.valueOf(2500.0))
                .isActive(true)
                .build();
        Mockito.when(accountFactory.createSavingsAccount(any(), anyString(), anyLong())).thenReturn(mockAccount);
        Mockito.when(accountRepository.save(any())).thenReturn(mockAccount);

        AccountResponse response = accountService.createSavingsAccount(savingsRequest);
        assertNotNull(response);
        assertEquals(1001L, response.getAccountNumber());
        assertEquals("Amit Verma", response.getName());
    }

    @Test
    void testCreateSavingsAccount_ManagerOverrideName_Success() {
        // Logged in as manager1
        UserContext context = UserContext.builder()
                .userId(6L)
                .loginId("manager1")
                .role("MANAGER")
                .build();
        UserContextHolder.setContext(context);

        // Requested KYC belongs to Priya Sharma, even though request name was Amit Verma
        Map<String, Object> priyaUser = new HashMap<>();
        priyaUser.put("username", "Priya Sharma");

        Mockito.when(userClient.getUserByKyc("696673394911")).thenReturn(priyaUser);
        Mockito.when(aadharClient.verifyAadhar("696673394911")).thenReturn(true);
        Mockito.when(accountRepository.existsByNameAndDob("Priya Sharma", "1990-05-12")).thenReturn(false);
        Mockito.when(accountRepository.generateAccountNumber()).thenReturn(1002L);

        Account mockAccount = Account.builder()
                .accountNumber(1002L)
                .name("Priya Sharma")
                .balance(java.math.BigDecimal.valueOf(2500.0))
                .isActive(true)
                .build();
        Mockito.when(accountFactory.createSavingsAccount(any(), anyString(), anyLong())).thenReturn(mockAccount);
        Mockito.when(accountRepository.save(any())).thenReturn(mockAccount);

        AccountResponse response = accountService.createSavingsAccount(savingsRequest);
        assertNotNull(response);
        assertEquals("Priya Sharma", response.getName()); // Verified that it got overridden!
    }
}
