package com.gdb.account.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${app.services.users-url:http://users-service:8003}")
    private String usersServiceUrl;

    public String getUsername(String loginId) {
        String url = usersServiceUrl + "/internal/v1/users/" + loginId + "/status";
        try {
            log.info("Calling Users Service to fetch status/username for: {}", loginId);
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("username")) {
                return (String) response.get("username");
            }
        } catch (Exception e) {
            log.error("Error getting username from Users Service: {}", e.getMessage());
        }
        return null;
    }

    public String getKycNumber(String loginId) {
        String url = usersServiceUrl + "/internal/v1/users/" + loginId + "/status";
        try {
            log.info("Calling Users Service to fetch status/kyc_number for: {}", loginId);
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("kyc_number")) {
                return (String) response.get("kyc_number");
            }
        } catch (Exception e) {
            log.error("Error getting kyc_number from Users Service: {}", e.getMessage());
        }
        return null;
    }

    public Map getUserByKyc(String kycNumber) {
        String url = usersServiceUrl + "/internal/v1/users/kyc/" + kycNumber;
        try {
            log.info("Calling Users Service to fetch user by KYC number: {}", kycNumber);
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("Error getting user by KYC number from Users Service: {}", e.getMessage());
        }
        return null;
    }
}
