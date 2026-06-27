package com.gdb.settings.service.impl;

import com.gdb.settings.domain.Settings;
import com.gdb.settings.dto.SettingsDto;
import com.gdb.settings.repository.SettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class SettingsServiceImplTest {

    @Mock
    private SettingsRepository settingsRepository;

    @InjectMocks
    private SettingsServiceImpl settingsService;

    private Settings testSettings;

    @BeforeEach
    void setUp() {
        testSettings = new Settings();
        testSettings.setUserId("user123");
        testSettings.setTheme("DARK");
        testSettings.setLanguage("fr");
        testSettings.setEmailNotifications(true);
        testSettings.setSmsNotifications(true);
        testSettings.setTwoFactorAuthEnabled(true);
        testSettings.setCompactMode(true);
        testSettings.setSidebarCollapsed(true);
    }

    @Test
    void testGetSettings_Existing() {
        Mockito.when(settingsRepository.findById("user123")).thenReturn(Optional.of(testSettings));

        SettingsDto dto = settingsService.getSettings("user123");

        assertNotNull(dto);
        assertEquals("user123", dto.getUserId());
        assertEquals("DARK", dto.getTheme());
        assertTrue(dto.isCompactMode());
        assertTrue(dto.isSidebarCollapsed());
    }

    @Test
    void testGetSettings_Default() {
        Mockito.when(settingsRepository.findById("newuser")).thenReturn(Optional.empty());
        Mockito.when(settingsRepository.save(any(Settings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SettingsDto dto = settingsService.getSettings("newuser");

        assertNotNull(dto);
        assertEquals("newuser", dto.getUserId());
        assertEquals("SYSTEM", dto.getTheme()); // default
        assertFalse(dto.isCompactMode()); // default
        assertFalse(dto.isSidebarCollapsed()); // default
    }

    @Test
    void testUpdateSettings() {
        Mockito.when(settingsRepository.findById("user123")).thenReturn(Optional.of(testSettings));
        Mockito.when(settingsRepository.save(any(Settings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SettingsDto requestDto = new SettingsDto();
        requestDto.setUserId("user123");
        requestDto.setTheme("LIGHT");
        requestDto.setLanguage("en");
        requestDto.setEmailNotifications(false);
        requestDto.setSmsNotifications(false);
        requestDto.setTwoFactorAuthEnabled(false);
        requestDto.setCompactMode(false);
        requestDto.setSidebarCollapsed(false);

        SettingsDto responseDto = settingsService.updateSettings("user123", requestDto);

        assertNotNull(responseDto);
        assertEquals("LIGHT", responseDto.getTheme());
        assertFalse(responseDto.isCompactMode());
        assertFalse(responseDto.isSidebarCollapsed());
    }
}
