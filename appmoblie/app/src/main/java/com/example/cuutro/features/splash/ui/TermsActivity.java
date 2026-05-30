package com.example.cuutro.features.splash.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.navigation.ui.CaptainNavigationActivity;
import com.example.cuutro.features.navigation.ui.NavActivity;
import com.example.cuutro.features.splash.data.TermsConsentManager;

public class TermsActivity extends AppCompatActivity {

    private CheckBox agreeCheckBox;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_terms);
        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        agreeCheckBox = findViewById(R.id.cb_terms_agree);
        continueButton = findViewById(R.id.btn_terms_continue);
        setupActions();
        updateContinueButtonState();
    }

    private void setupActions() {
        if (agreeCheckBox != null) {
            agreeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                    updateContinueButtonState()
            );
        }

        if (continueButton != null) {
            continueButton.setOnClickListener(v -> handleContinue());
        }
    }

    private void handleContinue() {
        if (agreeCheckBox == null || !agreeCheckBox.isChecked()) {
            Toast.makeText(this, R.string.terms_accept_required, Toast.LENGTH_SHORT).show();
            return;
        }

        TermsConsentManager.setTermsAccepted(this, true);
        navigateToHomeActivityByRole();
    }

    private void updateContinueButtonState() {
        if (continueButton == null) {
            return;
        }
        boolean enabled = agreeCheckBox != null && agreeCheckBox.isChecked();
        continueButton.setEnabled(enabled);
        continueButton.setAlpha(enabled ? 1f : 0.6f);
    }

    private void navigateToHomeActivityByRole() {
        Class<?> destination = NavActivity.class;
        MyApp app = (MyApp) getApplication();
        if (app.getAppContainer() != null) {
            AuthRepository authRepository = app.getAppContainer().getAuthRepository();
            if (authRepository != null
                    && authRepository.hasActiveSession()
                    && authRepository.isCurrentRoleCaptain()) {
                destination = CaptainNavigationActivity.class;
            }
        }
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
