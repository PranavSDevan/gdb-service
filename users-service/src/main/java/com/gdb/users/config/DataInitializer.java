package com.gdb.users.config;

import com.gdb.users.constants.UserConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Initializes default users on startup and fixes any corrupted password hashes.
 * This runs every time the application starts, but only modifies data if
 * needed.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    /** The standard password used for all default/seeded users. */
    private static final String DEFAULT_PASSWORD = "Welcome@1";

    @Override
    public void run(String... args) {
        ensureDefaultUsersExist();
        fixAllPasswords();
    }

    /**
     * Ensures that the default users referenced in the frontend quick-login
     * buttons exist in the database.
     * Default credentials all use password: Welcome@1
     */
    private void ensureDefaultUsersExist() {
        createUserIfNotExists("System Admin", "admin", UserConstants.ROLE_ADMIN);
        createUserIfNotExists("John Doe", "john.doe", UserConstants.ROLE_TELLER);
        createUserIfNotExists("Teller User", "teller", UserConstants.ROLE_TELLER);
        createUserIfNotExists("Manager User", "manager.manager", UserConstants.ROLE_MANAGER);
    }

    private void createUserIfNotExists(String username, String loginId, String role) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE login_id = ?", Integer.class, loginId);

        if (count == null || count == 0) {
            log.info("Creating default user '{}' (login: {}, password: {})", username, loginId, DEFAULT_PASSWORD);
            String hashedPassword = BCrypt.hashpw(DEFAULT_PASSWORD, BCrypt.gensalt(UserConstants.BCRYPT_SALT_ROUNDS));
            jdbcTemplate.update(
                    "INSERT INTO users (username, login_id, password, role, is_active) VALUES (?, ?, ?, ?, ?)",
                    username, loginId, hashedPassword, role, true);
            log.info("Default user '{}' created successfully.", loginId);
        } else {
            log.debug("User '{}' already exists.", loginId);
        }
    }

    /**
     * Resets ALL user passwords to the standard default password (Welcome@1).
     * This ensures the Flyway-seeded admin (whose hash was for 'password')
     * and any other users are aligned with what the frontend expects.
     */
    private void fixAllPasswords() {
        var users = jdbcTemplate.queryForList(
                "SELECT user_id, login_id, password, kyc_number FROM users");

        String validHash = BCrypt.hashpw(DEFAULT_PASSWORD, BCrypt.gensalt(UserConstants.BCRYPT_SALT_ROUNDS));

        for (var user : users) {
            String password = (String) user.get("password");
            String loginId = (String) user.get("login_id");
            String kycNumber = (String) user.get("kyc_number");

            // Assign predefined KYC numbers for standard users to align with accounts seed data
            String predefinedKyc = null;
            if ("admin".equalsIgnoreCase(loginId)) {
                predefinedKyc = "456789012345";
            } else if ("john.doe".equalsIgnoreCase(loginId)) {
                predefinedKyc = "123456789012";
            } else if ("teller".equalsIgnoreCase(loginId)) {
                predefinedKyc = "123456789013";
            } else if ("teller1".equalsIgnoreCase(loginId)) {
                predefinedKyc = "234567890123";
            } else if ("teller2".equalsIgnoreCase(loginId)) {
                predefinedKyc = "345678901234";
            } else if ("manager1".equalsIgnoreCase(loginId)) {
                predefinedKyc = "567890123456";
            } else if ("manager2".equalsIgnoreCase(loginId)) {
                predefinedKyc = "678901234567";
            }

            if (predefinedKyc != null) {
                if (!predefinedKyc.equals(kycNumber)) {
                    jdbcTemplate.update("UPDATE users SET kyc_number = ? WHERE user_id = ?", predefinedKyc, user.get("user_id"));
                    log.info("Assigned predefined KYC number {} to user '{}'", predefinedKyc, loginId);
                }
            } else if (kycNumber == null || kycNumber.isBlank()) {
                String newKyc = generateRandomKycNumber();
                jdbcTemplate.update("UPDATE users SET kyc_number = ? WHERE user_id = ?", newKyc, user.get("user_id"));
                log.info("Generated new KYC number {} for user '{}'", newKyc, loginId);
            }


            boolean needsReset = false;

            if (password == null || (!password.startsWith("$2a$") && !password.startsWith("$2b$"))) {
                log.warn("User '{}' has an invalid password hash. Resetting.", loginId);
                needsReset = true;
            } else {
                // Check if the password already matches the default
                try {
                    if (!BCrypt.checkpw(DEFAULT_PASSWORD, password)) {
                        log.info("User '{}' password does not match default. Resetting to '{}'.", loginId, DEFAULT_PASSWORD);
                        needsReset = true;
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("User '{}' has a corrupted BCrypt hash. Resetting.", loginId);
                    needsReset = true;
                }
            }

            if (needsReset) {
                jdbcTemplate.update("UPDATE users SET password = ? WHERE login_id = ?", validHash, loginId);
                log.info("Password for user '{}' has been reset to default.", loginId);
            }
        }
    }

    private String generateRandomKycNumber() {
        java.util.Random random = new java.util.Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
