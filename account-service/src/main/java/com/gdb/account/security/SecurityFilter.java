package com.gdb.account.security;

import com.gdb.account.client.AuthClient;
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

        // ENTERPRISE FIX: Allow OPTIONS to bypass authorization checks without
        // appending duplicate headers that conflict with gateway configurations.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Allow Swagger/OpenAPI, health, and internal service-to-service APIs without auth
        if (path.contains("/api-docs") || path.contains("/swagger-ui") || path.contains("/docs")
                || path.equals("/health")
                || path.startsWith("/api/v1/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            AuthClient.TokenValidationResponse validationResponse = authClient.validateToken(token);

            if (validationResponse.isValid()) {
                UserContext context = UserContext.builder()
                        .userId(validationResponse.getUserId())
                        .loginId(validationResponse.getLoginId())
                        .role(validationResponse.getRole())
                        .build();
                UserContextHolder.setContext(context);
                log.debug("Authenticated user: {} with role: {}", context.getLoginId(), context.getRole());

                // ENTERPRISE FIX: Stripped out manual origin headers to let the edge gateway manage them.
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