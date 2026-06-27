package com.gdb.users.service.impl;

import com.gdb.users.domain.model.User;
import com.gdb.users.dto.request.AddUserRequest;
import com.gdb.users.exception.UserAlreadyExistsException;
import com.gdb.users.exception.UserNotFoundException;
import com.gdb.users.repository.UserRepository;
import com.gdb.users.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .username("John Doe")
                .loginId("john.doe")
                .password("hashed_pwd")
                .role("TELLER")
                .isActive(true)
                .kycNumber("123456789012")
                .build();
    }

    @Test
    void testAddUser_Success() {
        AddUserRequest request = new AddUserRequest();
        request.setLoginId("new.user");
        request.setUsername("New User");
        request.setPassword("Password@123");
        request.setRole("TELLER");

        Mockito.when(userRepository.existsByLoginId("new.user")).thenReturn(false);
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setUserId(2L);
            return saved;
        });

        User result = userService.addUser(request);

        assertNotNull(result);
        assertEquals("new.user", result.getLoginId());
        assertNotNull(result.getKycNumber());
        assertEquals(12, result.getKycNumber().length());
        assertTrue(result.getIsActive());
        Mockito.verify(userRepository).save(any(User.class));
    }

    @Test
    void testAddUser_Duplicate() {
        AddUserRequest request = new AddUserRequest();
        request.setLoginId("john.doe");

        Mockito.when(userRepository.existsByLoginId("john.doe")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.addUser(request));
    }

    @Test
    void testGetUserByKycNumber_Success() {
        Mockito.when(userRepository.findByKycNumber("123456789012")).thenReturn(Optional.of(testUser));

        User result = userService.getUserByKycNumber("123456789012");

        assertNotNull(result);
        assertEquals("john.doe", result.getLoginId());
        assertEquals("123456789012", result.getKycNumber());
    }

    @Test
    void testGetUserByKycNumber_NotFound() {
        Mockito.when(userRepository.findByKycNumber("999999999999")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByKycNumber("999999999999"));
    }
}
