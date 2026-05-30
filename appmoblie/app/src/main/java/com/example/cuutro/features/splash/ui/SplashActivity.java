package com.example.cuutro.features.splash.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.navigation.ui.CaptainNavigationActivity;
import com.example.cuutro.features.navigation.ui.NavActivity;
import com.example.cuutro.features.splash.data.TermsConsentManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2000L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable navigateToNextScreen = () -> {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        if (!TermsConsentManager.hasAcceptedTerms(SplashActivity.this)) {
            startActivity(new Intent(SplashActivity.this, TermsActivity.class));
            finish();
            return;
        }
        Class<?> destination = resolveDestinationByRole();
        startActivity(new Intent(SplashActivity.this, destination));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        handler.postDelayed(navigateToNextScreen, SPLASH_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(navigateToNextScreen);
        super.onDestroy();
    }

    private Class<?> resolveDestinationByRole() {
        MyApp app = (MyApp) getApplication();
        if (app.getAppContainer() == null) {
            return NavActivity.class;
        }
        AuthRepository authRepository = app.getAppContainer().getAuthRepository();
        if (authRepository != null
                && authRepository.hasActiveSession()
                && authRepository.isCurrentRoleCaptain()) {
            return CaptainNavigationActivity.class;
        }
        return NavActivity.class;
    }
}
