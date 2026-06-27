package com.gdb.settings.controller;

import com.gdb.settings.dto.SettingsDto;
import com.gdb.settings.service.SettingsService;
import com.gdb.settings.security.SecurityUtils;
import com.gdb.settings.security.UserContext;
import com.gdb.settings.security.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/{userId}")
    public ResponseEntity<SettingsDto> getSettings(@PathVariable String userId) {
        SecurityUtils.checkAnyStaffRole();
        checkTellerAccess(userId);
        return ResponseEntity.ok(settingsService.getSettings(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<SettingsDto> updateSettings(@PathVariable String userId, @RequestBody SettingsDto settingsDto) {
        SecurityUtils.checkAnyStaffRole();
        checkTellerAccess(userId);
        return ResponseEntity.ok(settingsService.updateSettings(userId, settingsDto));
    }

    private void checkTellerAccess(String targetUserId) {
        UserContext context = UserContextHolder.getContext();
        if (context != null && "TELLER".equalsIgnoreCase(context.getRole())) {
            if (!context.getUserId().toString().equals(targetUserId)) {
                throw new RuntimeException("ACCESS_DENIED");
            }
        }
    }
}
