package com.gdb.ai.controller;

import com.gdb.ai.dto.AiRequestDto;
import com.gdb.ai.dto.AiResponseDto;
import com.gdb.ai.service.AiService;
import com.gdb.ai.security.SecurityUtils;
import com.gdb.ai.security.UserContext;
import com.gdb.ai.security.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<AiResponseDto> chat(@RequestBody AiRequestDto request) {
        SecurityUtils.checkAnyStaffRole();
        checkTellerAccess(request);
        AiResponseDto response = aiService.getResponse(request.getQuery(), request.getUserId());
        return ResponseEntity.ok(response);
    }

    private void checkTellerAccess(AiRequestDto request) {
        UserContext context = UserContextHolder.getContext();
        if (context != null && "TELLER".equalsIgnoreCase(context.getRole())) {
            if (!context.getUserId().toString().equals(request.getUserId())) {
                throw new RuntimeException("ACCESS_DENIED");
            }
        }
    }
}
