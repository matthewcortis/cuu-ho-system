package com.example.cuutro.features.splash.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cuutro.R;
import com.example.cuutro.features.navigation.ui.NavActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2000L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable navigateToNextScreen = () -> {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        startActivity(new Intent(SplashActivity.this, NavActivity.class));
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
}
