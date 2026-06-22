package com.gdb.users.service.impl;

import com.gdb.users.constants.UserConstants;
import com.gdb.users.domain.model.User;
import com.gdb.users.dto.request.AddUserRequest;
import com.gdb.users.dto.request.EditUserRequest;
import com.gdb.users.dto.response.VerifyCredentialsResponse;
import com.gdb.users.exception.UserNotFoundException;
import com.gdb.users.exception.UserAlreadyExistsException;
import com.gdb.users.repository.UserRepository;
import com.gdb.users.service.AuditService;
import com.gdb.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    // REMOVED: Auto-commit works perfectly here for a single row insert
    public User addUser(AddUserRequest request) {
        log.info("Adding new user with loginId: {}", request.getLoginId());

        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new UserAlreadyExistsException("User with loginId " + request.getLoginId() + " already exists");
        }

        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(UserConstants.BCRYPT_SALT_ROUNDS));
        String role = request.getRole() != null ? request.getRole() : UserConstants.ROLE_TELLER;

        User user = User.builder()
                .username(request.getUsername())
                .loginId(request.getLoginId())
                .password(hashedPassword)
                .role(role)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        try {
            auditService.logAction(savedUser.getUserId(), UserConstants.ACTION_CREATE, null, savedUser);
        } catch (Exception e) {
            log.error("User saved successfully, but tracking log failed: {}", e.getMessage());
        }

        return savedUser;
    }

    @Override
    @Transactional("transactionManager") // KEEP THIS: Manages multi-step updates and fetches together safely
    public User updateUser(String loginId, EditUserRequest request) {
        log.info("Updating user with loginId: {}", loginId);

        User existingUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UserNotFoundException("User not found with loginId: " + loginId));

        User.UserBuilder userBuilder = User.builder().loginId(loginId);
        boolean updated = false;

        if (request.getUsername() != null) {
            userBuilder.username(request.getUsername());
            updated = true;
        }
        if (request.getPassword() != null) {
            String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(UserConstants.BCRYPT_SALT_ROUNDS));
            userBuilder.password(hashedPassword);
            updated = true;
        }
        if (request.getRole() != null) {
            userBuilder.role(request.getRole());
            updated = true;
        }

        if (updated) {
            User userToUpdate = userBuilder.build();
            userRepository.update(userToUpdate);
            User updatedUser = userRepository.findByLoginId(loginId).get();

            try {
                auditService.logAction(updatedUser.getUserId(), UserConstants.ACTION_UPDATE, existingUser, updatedUser);
            } catch (Exception e) {
                log.error("User updated successfully, but tracking log failed: {}", e.getMessage());
            }
            return updatedUser;
        }

        return existingUser;
    }

    @Override
    public User getUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UserNotFoundException("User not found with loginId: " + loginId));
    }

    @Override
    public List<User> listUsers(String role, Boolean isActive) {
        return userRepository.findAll(role, isActive);
    }

    @Override
    // REMOVED: Safe to remove for single database modification executions
    public void activateUser(String loginId) {
        User user = getUserByLoginId(loginId);
        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalStateException("User is already active");
        }
        userRepository.updateStatus(loginId, true);

        try {
            auditService.logAction(user.getUserId(), UserConstants.ACTION_ACTIVATE, "inactive", "active");
        } catch (Exception e) {
            log.error("User status activated, but logging failed: {}", e.getMessage());
        }
    }

    @Override
    // REMOVED: Safe to remove for single database modification executions
    public void inactivateUser(String loginId) {
        User user = getUserByLoginId(loginId);
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new IllegalStateException("User is already inactive");
        }
        userRepository.updateStatus(loginId, false);

        try {
            auditService.logAction(user.getUserId(), UserConstants.ACTION_INACTIVATE, "active", "inactive");
        } catch (Exception e) {
            log.error("User status inactivated, but logging failed: {}", e.getMessage());
        }
    }

    @Override
    public VerifyCredentialsResponse verifyCredentials(String loginId, String password) {
        Optional<User> userOpt = userRepository.findByLoginId(loginId);

        if (userOpt.isEmpty()) {
            return VerifyCredentialsResponse.builder().isValid(false).isActive(false).build();
        }

        User user = userOpt.get();
        if (Boolean.FALSE.equals(user.getIsActive())) {
            return VerifyCredentialsResponse.builder()
                    .isValid(false)
                    .userId(user.getUserId())
                    .role(user.getRole())
                    .isActive(false)
                    .build();
        }

        boolean passwordMatch;
        try {
            passwordMatch = BCrypt.checkpw(password, user.getPassword());
        } catch (IllegalArgumentException e) {
            log.error("Invalid BCrypt hash for user: {}.", loginId, e);
            return VerifyCredentialsResponse.builder()
                    .isValid(false)
                    .userId(user.getUserId())
                    .role(user.getRole())
                    .isActive(true)
                    .build();
        }

        return VerifyCredentialsResponse.builder()
                .isValid(passwordMatch)
                .userId(user.getUserId())
                .role(user.getRole())
                .isActive(true)
                .build();
    }
}