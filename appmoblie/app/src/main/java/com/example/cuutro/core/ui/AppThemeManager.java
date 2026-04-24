package com.example.cuutro.core.ui;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

public final class AppThemeManager {

    private static final String PREFS_NAME = "cuutro_ui_settings";
    private static final String KEY_THEME_MODE = "theme_mode";

    private AppThemeManager() {
    }

    public static void applySavedTheme(@NonNull Context context) {
        int savedMode = getSavedNightMode(context);
        if (AppCompatDelegate.getDefaultNightMode() != savedMode) {
            AppCompatDelegate.setDefaultNightMode(savedMode);
        }
    }

    public static boolean isDarkModeEnabled(@NonNull Context context) {
        return getSavedNightMode(context) == AppCompatDelegate.MODE_NIGHT_YES;
    }

    public static void setDarkModeEnabled(@NonNull Context context, boolean enabled) {
        int mode = enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putInt(KEY_THEME_MODE, mode).apply();
        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode);
        }
    }

    private static int getSavedNightMode(@NonNull Context context) {
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int mode = preferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO);
        if (mode == AppCompatDelegate.MODE_NIGHT_YES || mode == AppCompatDelegate.MODE_NIGHT_NO) {
            return mode;
        }
        return AppCompatDelegate.MODE_NIGHT_NO;
    }
}
