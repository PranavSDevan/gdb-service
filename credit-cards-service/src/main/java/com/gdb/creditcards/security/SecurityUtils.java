package com.gdb.creditcards.security;

import java.util.Set;

public class SecurityUtils {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_TELLER = "TELLER";

    private static final Set<String> ALL_STAFF = Set.of(ROLE_ADMIN, ROLE_MANAGER, ROLE_TELLER);
    private static final Set<String> OPERATIONAL_STAFF = Set.of(ROLE_ADMIN, ROLE_TELLER);
    private static final Set<String> MANAGEMENT_STAFF = Set.of(ROLE_ADMIN, ROLE_MANAGER);

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

    public static void checkAnyStaffRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null || !ALL_STAFF.contains(cleanRole(context.getRole()))) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }

    public static boolean isTeller() {
        UserContext context = UserContextHolder.getContext();
        return context != null &&
                ROLE_TELLER.equals(cleanRole(context.getRole()));
    }

    public static Long getCurrentUserId() {
        UserContext context = UserContextHolder.getContext();
        return context != null ? context.getUserId() : null;
    }
}
