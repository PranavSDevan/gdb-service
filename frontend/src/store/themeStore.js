/**
 * Theme Store
 * 
 * Manages application theme (light, dark, system) with persistence.
 * Automatically applies the theme to the document and handles system preference changes.
 */

import { create } from 'zustand';
import { persist } from 'zustand/middleware';

// Get system preference
const getSystemTheme = () => {
  if (typeof window !== 'undefined' && window.matchMedia) {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
  return 'light';
};

// Apply theme to document
const applyTheme = (theme) => {
  const root = document.documentElement;
  const effectiveTheme = theme === 'system' ? getSystemTheme() : theme;
  
  if (effectiveTheme === 'dark') {
    root.classList.add('dark');
  } else {
    root.classList.remove('dark');
  }
  
  // Store effective theme for CSS variables
  root.setAttribute('data-theme', effectiveTheme);
};

// Apply compact mode to document
const applyCompactMode = (compactMode) => {
  const root = document.documentElement;
  if (compactMode) {
    root.classList.add('compact');
    root.setAttribute('data-compact', 'true');
  } else {
    root.classList.remove('compact');
    root.removeAttribute('data-compact');
  }
};

export const useThemeStore = create(
  persist(
    (set, get) => ({
      // Theme can be 'light', 'dark', or 'system'
      theme: 'light',
      compactMode: false,
      sidebarCollapsed: false,
      
      // Set theme
      setTheme: (theme) => {
        set({ theme });
        applyTheme(theme);
      },

      // Set compact mode
      setCompactMode: (compactMode) => {
        set({ compactMode });
        applyCompactMode(compactMode);
      },

      // Set sidebar collapsed
      setSidebarCollapsed: (sidebarCollapsed) => {
        set({ sidebarCollapsed });
      },
      
      // Get effective theme (resolves 'system' to actual theme)
      getEffectiveTheme: () => {
        const { theme } = get();
        return theme === 'system' ? getSystemTheme() : theme;
      },
      
      // Initialize theme on app load
      initializeTheme: () => {
        const { theme, compactMode } = get();
        applyTheme(theme);
        applyCompactMode(compactMode);
        
        // Listen for system theme changes
        if (typeof window !== 'undefined' && window.matchMedia) {
          const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
          const handleChange = () => {
            const currentTheme = get().theme;
            if (currentTheme === 'system') {
              applyTheme('system');
            }
          };
          
          // Modern browsers
          if (mediaQuery.addEventListener) {
            mediaQuery.addEventListener('change', mediaQuery.matches ? handleChange : handleChange);
          } else {
            // Fallback for older browsers
            mediaQuery.addListener(handleChange);
          }
        }
      },
    }),
    {
      name: 'gdb-theme-storage',
      partialize: (state) => ({ 
        theme: state.theme,
        compactMode: state.compactMode,
        sidebarCollapsed: state.sidebarCollapsed
      }),
    }
  )
);

export default useThemeStore;
