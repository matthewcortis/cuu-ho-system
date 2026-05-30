package com.example.cuutro.features.splash.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public final class TermsConsentManager {

    private static final String PREFS_NAME = "cuutro_onboarding";
    private static final String KEY_TERMS_ACCEPTED = "terms_accepted";

    private TermsConsentManager() {
    }

    public static boolean hasAcceptedTerms(@NonNull Context context) {
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_TERMS_ACCEPTED, false);
    }

    public static void setTermsAccepted(@NonNull Context context, boolean accepted) {
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_TERMS_ACCEPTED, accepted).apply();
    }
}
