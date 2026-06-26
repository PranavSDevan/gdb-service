package com.gdb.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdb.ai.dto.AiResponseDto;
import com.gdb.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public AiResponseDto getResponse(String query, String userId) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("GEMINI_API_KEY is not defined in environment variables. Falling back to local NLP engine.");
            return getFallbackResponse(query);
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key="
                    + apiKey;

            String systemInstruction = "You are GDB Copilot, an AI virtual banking assistant for Global Digital Bank (GDB). "
                    + "Your job is to answer user queries politely and concisely. You should answer general banking questions, questions about GDB (Global Digital Bank), and questions about yourself (e.g. 'Are you a robot?', 'Who are you?', 'What is GDB?').\n"
                    + "If the user wants to navigate to a page, identify the page and return the matching route in the 'route' parameter of the JSON response. If no navigation is needed, keep 'route' null.\n"
                    + "Allowed routes and their pages:\n"
                    + "- '/credit-cards': For credit cards dashboard, card applications, outstanding amounts, card billing.\n"
                    + "- '/statements': For bank statements generation, previews, and downloads.\n"
                    + "- '/settings': For profile settings, configuration, passwords, notification settings, theme change, language change.\n"
                    + "- '/transactions': For domestic/international fund transfers, deposits, withdrawals, transaction history.\n"
                    + "- '/dashboard': For main overview dashboard, account balances, main page.\n\n"
                    + "User Query: " + query;

            Map<String, Object> textPart = Map.of("text", systemInstruction);
            Map<String, Object> partContainer = Map.of("parts", List.of(textPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(partContainer));

            // JSON Schema response format
            Map<String, Object> routeSchema = Map.of("type", "STRING");
            Map<String, Object> textSchema = Map.of("type", "STRING");
            Map<String, Object> properties = Map.of(
                    "text", textSchema,
                    "route", routeSchema);
            Map<String, Object> responseSchema = Map.of(
                    "type", "OBJECT",
                    "properties", properties,
                    "required", List.of("text"));
            Map<String, Object> generationConfig = Map.of(
                    "responseMimeType", "application/json",
                    "responseSchema", responseSchema);
            requestBody.put("generationConfig", generationConfig);

            log.info("Sending request to Gemini API model gemini-1.5-flash");
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);

            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    if (candidate.containsKey("content")) {
                        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                        if (content.containsKey("parts")) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                            if (parts != null && !parts.isEmpty()) {
                                String jsonText = (String) parts.get(0).get("text");
                                log.debug("Gemini response text: {}", jsonText);
                                return objectMapper.readValue(jsonText, AiResponseDto.class);
                            }
                        }
                    }
                }
            }

            log.warn("Unexpected Gemini API response structure. Falling back to local NLP.");
            return getFallbackResponse(query);

        } catch (Exception e) {
            log.error("Failed to query Gemini API: {}. Falling back to local NLP.", e.getMessage());
            return getFallbackResponse(query);
        }
    }

    private AiResponseDto getFallbackResponse(String query) {
        String normalized = query.toLowerCase().trim();
        if (normalized.contains("card") || normalized.contains("credit") || normalized.contains("apply")) {
            return new AiResponseDto(
                    "Sure! I'm opening the Credit Cards dashboard for you where you can apply for a new card or pay your bill.",
                    "/credit-cards");
        } else if (normalized.contains("statement") || normalized.contains("download") || normalized.contains("pdf")) {
            return new AiResponseDto(
                    "Certainly. Opening the Bank Statements manager where you can preview and download transaction files.",
                    "/statements");
        } else if (normalized.contains("setting") || normalized.contains("theme") || normalized.contains("password")) {
            return new AiResponseDto(
                    "Of course! I will navigate you to your Account Settings page to update settings.",
                    "/settings");
        } else if (normalized.contains("send") || normalized.contains("pay") || normalized.contains("transfer")
                || normalized.contains("money")) {
            return new AiResponseDto(
                    "Understood. Redirecting you to the Transaction Hub.",
                    "/transactions");
        } else if (normalized.contains("dashboard") || normalized.contains("home") || normalized.contains("overview")) {
            return new AiResponseDto(
                    "Navigating back to the main Account Overview dashboard.",
                    "/dashboard");
        } else if (normalized.contains("hello") || normalized.contains("hi") || normalized.contains("hey")) {
            return new AiResponseDto(
                    "Hello! I am GDB Copilot, your virtual banking assistant. Ask me to open a page (e.g. 'open settings') or help you with your account.",
                    null);
        } else if (normalized.contains("robot") || normalized.contains("bot") || normalized.contains("human") || normalized.contains("ai")) {
            return new AiResponseDto(
                    "I am GDB Copilot, an AI virtual banking assistant built to help you navigate the Global Digital Bank portal and answer banking queries.",
                    null);
        } else if (normalized.contains("gdb") || normalized.contains("bank about") || normalized.contains("this bank") || normalized.contains("about bank")) {
            return new AiResponseDto(
                    "Global Digital Bank (GDB) is a modern, secure online banking platform offering digital savings accounts, credit cards, statement downloads, and quick transfers.",
                    null);
        } else if (normalized.contains("yourself") || normalized.contains("who are you")
                || normalized.contains("what can you do")) {
            return new AiResponseDto(
                    "I am GDB Copilot, your virtual banking assistant for Global Digital Bank (GDB). I can help you navigate to different pages (like Settings, Statements, Credit Cards, and Transactions) and answer questions about the banking system.",
                    null);
        } else {
            return new AiResponseDto(
                    "I'm not fully sure how to assist with '" + query
                            + "'. However, I can help you navigate. Try saying 'open settings' or 'show credit cards'.",
                    null);
        }
    }
}
