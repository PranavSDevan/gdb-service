package com.gdb.auth.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface IntegrationService {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UserVerificationResponse {
        @JsonProperty("isValid")
        private boolean isValid;
        @JsonProperty("userId")
        private Long userId;
        private String role;
        @JsonProperty("isActive")
        private boolean isActive;
        @JsonProperty("username")
        private String username;
    }

    UserVerificationResponse verifyCredentials(String loginId, String password);

    UserVerificationResponse getUserStatus(String loginId);
}
