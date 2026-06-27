package com.gdb.transactions.security;

import java.util.Set;

/**
 * Enterprise Security Utilities for structural role checks inside Transactions Service.
 */
public class SecurityUtils {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_TELLER = "TELLER";

    // Standardized sets for role hierarchies
    private static final Set<String> ALL_STAFF = Set.of(ROLE_ADMIN, ROLE_MANAGER, ROLE_TELLER);
    private static final Set<String> OPERATIONAL_STAFF = Set.of(ROLE_ADMIN, ROLE_TELLER);
    private static final Set<String> MANAGEMENT_STAFF = Set.of(ROLE_ADMIN, ROLE_MANAGER);

    /**
     * Normalizes incoming role strings (e.g., "ROLE_TELLER" or "teller" -> "TELLER")
     */
    private static String cleanRole(String role) {
        if (role == null) return "";
        String cleaned = role.toUpperCase().trim();
        if (cleaned.startsWith("ROLE_")) {
            cleaned = cleaned.substring(5);
        }
        return cleaned;
    }

    public static void checkAdminRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null || !ROLE_ADMIN.equals(cleanRole(context.getRole()))) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }

    public static void checkStaffRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null || !OPERATIONAL_STAFF.contains(cleanRole(context.getRole()))) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }

    public static void checkManagerRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null || !MANAGEMENT_STAFF.contains(cleanRole(context.getRole()))) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }

    // FIX: Added missing cross-service method definition
    public static void checkAnyStaffRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null || !ALL_STAFF.contains(cleanRole(context.getRole()))) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }

    public static boolean isTeller() {
        UserContext context = UserContextHolder.getContext();
        return context != null && ROLE_TELLER.equals(cleanRole(context.getRole()));
    }

    public static String getCurrentLoginId() {
        UserContext context = UserContextHolder.getContext();
        return context != null ? context.getLoginId() : null;
    }
}