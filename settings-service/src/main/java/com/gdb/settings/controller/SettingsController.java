package com.gdb.settings.controller;

import com.gdb.settings.dto.SettingsDto;
import com.gdb.settings.service.SettingsService;
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
        return ResponseEntity.ok(settingsService.getSettings(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<SettingsDto> updateSettings(@PathVariable String userId, @RequestBody SettingsDto settingsDto) {
        return ResponseEntity.ok(settingsService.updateSettings(userId, settingsDto));
    }
}
