package com.gdb.account.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CompanyClient {

    private final RestTemplate restTemplate;
    private final String companyServiceUrl;

    public CompanyClient(RestTemplate restTemplate,
                         @Value("${external.company-service.url}") String companyServiceUrl) {
        this.restTemplate = restTemplate;
        this.companyServiceUrl = companyServiceUrl;
    }

    // FIX: Changed return type from boolean to CompanyVerificationResponse to carry data back
    public CompanyVerificationResponse verifyCompany(String registrationNumber) {
        String url = companyServiceUrl + "/api/v1/company/verify";
        log.info("Calling Company CRV Service at: {}", url);

        try {
            Map<String, String> request = new HashMap<>();
            request.put("registration_number", registrationNumber);

            CompanyVerificationResponse response = restTemplate.postForObject(
                    url,
                    request,
                    CompanyVerificationResponse.class);

            if (response != null) {
                log.info("Company verification response: {}", response);
                return response;
            } else {
                log.warn("Received null response from Company CRV Service");
                return null;
            }

        } catch (RestClientException e) {
            log.error("Error communicating with Company CRV Service", e);
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyVerificationResponse {
        @JsonProperty("registration_number")
        private String registrationNumber;

        @JsonProperty("is_valid")
        private boolean isValid;

        // FIX: Added company_name property mapping to capture the registry data
        @JsonProperty("company_name")
        @com.fasterxml.jackson.annotation.JsonAlias("companyName")
        private String companyName;

        private String status;
        private String message;
        private String timestamp;
    }
}