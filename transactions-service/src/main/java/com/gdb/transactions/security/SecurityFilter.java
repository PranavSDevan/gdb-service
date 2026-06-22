package com.gdb.transactions.security;

import com.gdb.transactions.client.AuthClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final AuthClient authClient;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ENTERPRISE FIX: Let Gateway handle OPTIONS preflight directly.
        // Simply allow them to pass through unhindered without duplicating headers.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Allow Swagger/OpenAPI and health endpoints without auth
        if (path.contains("/api-docs") || path.contains("/swagger-ui") || path.contains("/docs")
                || path.equals("/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            AuthClient.TokenValidationResponse validationResponse = authClient.validateToken(token);

            if (validationResponse.isValid()) {
                if (validationResponse.isRevoked()) {
                    log.warn("Revoked/Blacklisted token attempted access for path: {}", path);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked");
                    return;
                }

                UserContext context = UserContext.builder()
                        .userId(validationResponse.getUserId())
                        .loginId(validationResponse.getLoginId())
                        .role(validationResponse.getRole())
                        .build();
                UserContextHolder.setContext(context);
                log.debug("Authenticated user: {} with role: {}", context.getLoginId(), context.getRole());

                // ENTERPRISE FIX: Keeps the context thread alive safely while downstream processes,
                // then clears it cleanly without duplicating cross-origin origin headers.
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    UserContextHolder.clearContext();
                }
                return;

            } else {
                log.warn("Invalid token for path: {}", path);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }
        } else {
            log.warn("Missing Authorization header for path: {}", path);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization header");
            return;
        }
    }
}