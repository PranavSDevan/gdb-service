package com.gdb.settings.service;

import com.gdb.settings.dto.SettingsDto;

public interface SettingsService {
    SettingsDto getSettings(String userId);
    SettingsDto updateSettings(String userId, SettingsDto settingsDto);
}
