package com.gdb.settings.dto;

import lombok.Data;

@Data
public class SettingsDto {
    private String userId;
    private String theme;
    private String language;
    private boolean emailNotifications;
    private boolean smsNotifications;
    private boolean twoFactorAuthEnabled;
    private boolean compactMode;
    private boolean sidebarCollapsed;
    private String dateFormat;
    private String currency;
}

