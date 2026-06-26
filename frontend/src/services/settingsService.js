import { settingsApi } from './apiConfig';

const USE_REAL_API = true;

export const settingsService = {
  getSettings: async (userId) => {
    if (!USE_REAL_API) {
      // Return standard mock fallback directly
      return {
        userId,
        theme: 'SYSTEM',
        language: 'en',
        emailNotifications: true,
        smsNotifications: false,
        twoFactorAuthEnabled: false
      };
    }
    
    try {
      const response = await settingsApi.get(`/api/v1/settings/${userId}`);
      return response.data;
    } catch (error) {
      console.warn("Failed fetching settings from backend, falling back to mock", error);
      return {
        userId,
        theme: 'SYSTEM',
        language: 'en',
        emailNotifications: true,
        smsNotifications: false,
        twoFactorAuthEnabled: false
      };
    }
  },

  updateSettings: async (userId, settingsData) => {
    if (!USE_REAL_API) {
      return { ...settingsData, userId };
    }

    try {
      const response = await settingsApi.put(`/api/v1/settings/${userId}`, settingsData);
      return response.data;
    } catch (error) {
      console.error("Failed saving settings to backend", error);
      throw new Error(error.response?.data?.message || "Failed to save settings");
    }
  }
};
