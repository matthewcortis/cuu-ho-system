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

public class RegisterActivity extends AppCompatActivity {

    public static final String EXTRA_REGISTERED_USERNAME = "registered_username";

    private EditText fullNameEditText;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;
    private View loginView;
    private CharSequence defaultRegisterButtonText;
    private AuthRepository authRepository;
    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
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
        fullNameEditText = findViewById(R.id.et_register_full_name);
        usernameEditText = findViewById(R.id.et_register_username);
        emailEditText = findViewById(R.id.et_register_email);
        passwordEditText = findViewById(R.id.et_register_password);
        confirmPasswordEditText = findViewById(R.id.et_register_confirm_password);
        registerButton = findViewById(R.id.btn_register);
        loginView = findViewById(R.id.tv_register_log_in);
        defaultRegisterButtonText = registerButton != null
                ? registerButton.getText()
                : getString(R.string.signup_cta);
    }

    private void setupActions() {
        if (registerButton != null) {
            registerButton.setOnClickListener(v -> submitRegister());
        }

        if (loginView != null) {
            loginView.setOnClickListener(v -> finish());
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

        if (fullNameEditText != null) {
            fullNameEditText.addTextChangedListener(clearErrorWatcher);
        }
        if (usernameEditText != null) {
            usernameEditText.addTextChangedListener(clearErrorWatcher);
        }
        if (emailEditText != null) {
            emailEditText.addTextChangedListener(clearErrorWatcher);
        }
        if (passwordEditText != null) {
            passwordEditText.addTextChangedListener(clearErrorWatcher);
        }
        if (confirmPasswordEditText != null) {
            confirmPasswordEditText.addTextChangedListener(clearErrorWatcher);
        }
    }

    private void clearErrors() {
        if (fullNameEditText != null) {
            fullNameEditText.setError(null);
        }
        if (usernameEditText != null) {
            usernameEditText.setError(null);
        }
        if (emailEditText != null) {
            emailEditText.setError(null);
        }
        if (passwordEditText != null) {
            passwordEditText.setError(null);
        }
        if (confirmPasswordEditText != null) {
            confirmPasswordEditText.setError(null);
        }
    }

    private void submitRegister() {
        if (isLoading || authRepository == null) {
            return;
        }

        String fullName = getTrimmedText(fullNameEditText);
        String username = getTrimmedText(usernameEditText);
        String email = getTrimmedText(emailEditText);
        String password = getTrimmedText(passwordEditText);
        String confirmPassword = getTrimmedText(confirmPasswordEditText);

        if (fullName.isEmpty()) {
            if (fullNameEditText != null) {
                fullNameEditText.setError(getString(R.string.signup_validation_full_name_required));
                fullNameEditText.requestFocus();
            }
            return;
        }

        if (username.isEmpty()) {
            if (usernameEditText != null) {
                usernameEditText.setError(getString(R.string.signup_validation_username_required));
                usernameEditText.requestFocus();
            }
            return;
        }

        if (email.isEmpty()) {
            if (emailEditText != null) {
                emailEditText.setError(getString(R.string.signup_validation_email_required));
                emailEditText.requestFocus();
            }
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (emailEditText != null) {
                emailEditText.setError(getString(R.string.signup_validation_email_invalid));
                emailEditText.requestFocus();
            }
            return;
        }

        if (password.isEmpty()) {
            if (passwordEditText != null) {
                passwordEditText.setError(getString(R.string.signup_validation_password_required));
                passwordEditText.requestFocus();
            }
            return;
        }

        if (password.length() < 6) {
            if (passwordEditText != null) {
                passwordEditText.setError(getString(R.string.signup_validation_password_too_short));
                passwordEditText.requestFocus();
            }
            return;
        }

        if (confirmPassword.isEmpty()) {
            if (confirmPasswordEditText != null) {
                confirmPasswordEditText.setError(getString(R.string.signup_validation_confirm_password_required));
                confirmPasswordEditText.requestFocus();
            }
            return;
        }

        if (!password.equals(confirmPassword)) {
            if (confirmPasswordEditText != null) {
                confirmPasswordEditText.setError(getString(R.string.signup_validation_password_mismatch));
                confirmPasswordEditText.requestFocus();
            }
            return;
        }

        setLoading(true);
        authRepository.register(fullName, username, email, password, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setLoading(false);
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_REGISTERED_USERNAME, username);
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(RegisterActivity.this, R.string.signup_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setLoading(false);
                Toast.makeText(RegisterActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        if (fullNameEditText != null) {
            fullNameEditText.setEnabled(!loading);
        }
        if (usernameEditText != null) {
            usernameEditText.setEnabled(!loading);
        }
        if (emailEditText != null) {
            emailEditText.setEnabled(!loading);
        }
        if (passwordEditText != null) {
            passwordEditText.setEnabled(!loading);
        }
        if (confirmPasswordEditText != null) {
            confirmPasswordEditText.setEnabled(!loading);
        }
        if (loginView != null) {
            loginView.setEnabled(!loading);
            loginView.setAlpha(loading ? 0.5f : 1f);
        }
        if (registerButton != null) {
            registerButton.setEnabled(!loading);
            registerButton.setText(loading ? getString(R.string.signup_loading) : defaultRegisterButtonText);
            registerButton.setAlpha(loading ? 0.8f : 1f);
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
