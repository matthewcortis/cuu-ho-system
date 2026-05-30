package com.example.cuutro.features.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;

public class ForgotPasswordActivity extends AppCompatActivity {

    public static final String EXTRA_RESET_EMAIL = "reset_email";

    private EditText emailEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button submitButton;
    private View backToLoginView;
    private CharSequence defaultSubmitButtonText;
    private AuthRepository authRepository;
    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MyApp app = (MyApp) getApplication();
        authRepository = app.getAppContainer().getAuthRepository();

        initViews();
        setupActions();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.et_forgot_email);
        newPasswordEditText = findViewById(R.id.et_forgot_new_password);
        confirmPasswordEditText = findViewById(R.id.et_forgot_confirm_password);
        submitButton = findViewById(R.id.btn_forgot_submit);
        backToLoginView = findViewById(R.id.tv_forgot_back_to_login);
        defaultSubmitButtonText = submitButton != null
                ? submitButton.getText()
                : getString(R.string.forgot_password_cta);
    }

    private void setupActions() {
        if (submitButton != null) {
            submitButton.setOnClickListener(v -> submitResetPassword());
        }

        if (backToLoginView != null) {
            backToLoginView.setOnClickListener(v -> finish());
        }

        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearErrors();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No-op.
            }
        };

        if (emailEditText != null) {
            emailEditText.addTextChangedListener(clearErrorWatcher);
        }
        if (newPasswordEditText != null) {
            newPasswordEditText.addTextChangedListener(clearErrorWatcher);
        }
        if (confirmPasswordEditText != null) {
            confirmPasswordEditText.addTextChangedListener(clearErrorWatcher);
        }
    }

    private void clearErrors() {
        if (emailEditText != null) {
            emailEditText.setError(null);
        }
        if (newPasswordEditText != null) {
            newPasswordEditText.setError(null);
        }
        if (confirmPasswordEditText != null) {
            confirmPasswordEditText.setError(null);
        }
    }

    private void submitResetPassword() {
        if (isLoading || authRepository == null) {
            return;
        }

        String email = getTrimmedText(emailEditText);
        String newPassword = getTrimmedText(newPasswordEditText);
        String confirmPassword = getTrimmedText(confirmPasswordEditText);

        if (email.isEmpty()) {
            if (emailEditText != null) {
                emailEditText.setError(getString(R.string.forgot_password_validation_email_required));
                emailEditText.requestFocus();
            }
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (emailEditText != null) {
                emailEditText.setError(getString(R.string.forgot_password_validation_email_invalid));
                emailEditText.requestFocus();
            }
            return;
        }

        if (newPassword.isEmpty()) {
            if (newPasswordEditText != null) {
                newPasswordEditText.setError(getString(R.string.forgot_password_validation_new_password_required));
                newPasswordEditText.requestFocus();
            }
            return;
        }

        if (newPassword.length() < 6) {
            if (newPasswordEditText != null) {
                newPasswordEditText.setError(getString(R.string.forgot_password_validation_new_password_short));
                newPasswordEditText.requestFocus();
            }
            return;
        }

        if (confirmPassword.isEmpty()) {
            if (confirmPasswordEditText != null) {
                confirmPasswordEditText.setError(getString(R.string.forgot_password_validation_confirm_required));
                confirmPasswordEditText.requestFocus();
            }
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            if (confirmPasswordEditText != null) {
                confirmPasswordEditText.setError(getString(R.string.forgot_password_validation_confirm_mismatch));
                confirmPasswordEditText.requestFocus();
            }
            return;
        }

        setLoading(true);
        authRepository.forgotPassword(email, newPassword, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setLoading(false);
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_RESET_EMAIL, email);
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(
                        ForgotPasswordActivity.this,
                        R.string.forgot_password_success,
                        Toast.LENGTH_SHORT
                ).show();
                finish();
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        if (emailEditText != null) {
            emailEditText.setEnabled(!loading);
        }
        if (newPasswordEditText != null) {
            newPasswordEditText.setEnabled(!loading);
        }
        if (confirmPasswordEditText != null) {
            confirmPasswordEditText.setEnabled(!loading);
        }
        if (backToLoginView != null) {
            backToLoginView.setEnabled(!loading);
            backToLoginView.setAlpha(loading ? 0.5f : 1f);
        }
        if (submitButton != null) {
            submitButton.setEnabled(!loading);
            submitButton.setText(loading ? getString(R.string.forgot_password_loading) : defaultSubmitButtonText);
            submitButton.setAlpha(loading ? 0.8f : 1f);
        }
    }

    @NonNull
    private String getTrimmedText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
