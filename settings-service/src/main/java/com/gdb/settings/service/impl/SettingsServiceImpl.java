package com.gdb.settings.service.impl;

import com.gdb.settings.domain.Settings;
import com.gdb.settings.dto.SettingsDto;
import com.gdb.settings.repository.SettingsRepository;
import com.gdb.settings.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final SettingsRepository settingsRepository;

    @Override
    public SettingsDto getSettings(String userId) {
        Settings settings = settingsRepository.findById(userId)
                .orElseGet(() -> {
                    Settings defaultSettings = new Settings();
                    defaultSettings.setUserId(userId);
                    return settingsRepository.save(defaultSettings);
                });
        return convertToDto(settings);
    }

    @Override
    public SettingsDto updateSettings(String userId, SettingsDto settingsDto) {
        Settings settings = settingsRepository.findById(userId)
                .orElseGet(() -> {
                    Settings s = new Settings();
                    s.setUserId(userId);
                    return s;
                });
        settings.setTheme(settingsDto.getTheme());
        settings.setLanguage(settingsDto.getLanguage());
        settings.setEmailNotifications(settingsDto.isEmailNotifications());
        settings.setSmsNotifications(settingsDto.isSmsNotifications());
        settings.setTwoFactorAuthEnabled(settingsDto.isTwoFactorAuthEnabled());
        settings.setCompactMode(settingsDto.isCompactMode());
        settings.setSidebarCollapsed(settingsDto.isSidebarCollapsed());
        settings.setDateFormat(settingsDto.getDateFormat());
        settings.setCurrency(settingsDto.getCurrency());
        Settings saved = settingsRepository.save(settings);
        return convertToDto(saved);
    }

    private SettingsDto convertToDto(Settings settings) {
        SettingsDto dto = new SettingsDto();
        dto.setUserId(settings.getUserId());
        dto.setTheme(settings.getTheme());
        dto.setLanguage(settings.getLanguage());
        dto.setEmailNotifications(settings.isEmailNotifications());
        dto.setSmsNotifications(settings.isSmsNotifications());
        dto.setTwoFactorAuthEnabled(settings.isTwoFactorAuthEnabled());
        dto.setCompactMode(settings.getCompactMode() != null && settings.getCompactMode());
        dto.setSidebarCollapsed(settings.getSidebarCollapsed() != null && settings.getSidebarCollapsed());
        dto.setDateFormat(settings.getDateFormat() != null ? settings.getDateFormat() : "DD/MM/YYYY");
        dto.setCurrency(settings.getCurrency() != null ? settings.getCurrency() : "INR");
        return dto;
    }

}
