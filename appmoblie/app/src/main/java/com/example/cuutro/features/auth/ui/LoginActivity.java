package com.example.cuutro.features.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.cuutro.features.navigation.ui.CaptainNavigationActivity;
import com.example.cuutro.features.navigation.ui.NavActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private CharSequence defaultLoginButtonText;
    private AuthRepository authRepository;
    private boolean isLoading;
    private final ActivityResultLauncher<Intent> registerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Intent data = result.getData();
                        if (result.getResultCode() != RESULT_OK || data == null) {
                            return;
                        }
                        String registeredUsername =
                                data.getStringExtra(RegisterActivity.EXTRA_REGISTERED_USERNAME);
                        if (registeredUsername != null && !registeredUsername.trim().isEmpty()
                                && emailEditText != null) {
                            emailEditText.setText(registeredUsername);
                            emailEditText.setSelection(registeredUsername.length());
                        }
                        if (passwordEditText != null) {
                            passwordEditText.setText("");
                        }
                        Toast.makeText(
                                this,
                                R.string.register_success_return_login,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
            );
    private final ActivityResultLauncher<Intent> forgotPasswordLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Intent data = result.getData();
                        if (result.getResultCode() != RESULT_OK || data == null) {
                            return;
                        }
                        String resetEmail =
                                data.getStringExtra(ForgotPasswordActivity.EXTRA_RESET_EMAIL);
                        if (resetEmail != null && !resetEmail.trim().isEmpty()
                                && emailEditText != null) {
                            emailEditText.setText(resetEmail);
                            emailEditText.setSelection(resetEmail.length());
                        }
                        if (passwordEditText != null) {
                            passwordEditText.setText("");
                        }
                        Toast.makeText(
                                this,
                                R.string.forgot_password_return_login,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
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

    @Override
    protected void onStart() {
        super.onStart();
        if (authRepository != null && authRepository.hasActiveSession()) {
            navigateToHomeActivityByRole();
        }
    }

    private void initViews() {
        emailEditText = findViewById(R.id.et_login_email);
        passwordEditText = findViewById(R.id.et_login_password);
        loginButton = findViewById(R.id.btn_login);
        defaultLoginButtonText = loginButton != null
                ? loginButton.getText()
                : getString(R.string.login_cta);
    }

    private void setupActions() {
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> submitLogin());
        }

        View forgotPasswordView = findViewById(R.id.tv_forgot_password);
        if (forgotPasswordView != null) {
            forgotPasswordView.setOnClickListener(v ->
                    forgotPasswordLauncher.launch(new Intent(this, ForgotPasswordActivity.class))
            );
        }

        View signUpView = findViewById(R.id.tv_sign_up);
        if (signUpView != null) {
            signUpView.setOnClickListener(v ->
                    registerLauncher.launch(new Intent(this, RegisterActivity.class))
            );
        }

        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (emailEditText != null) {
                    emailEditText.setError(null);
                }
                if (passwordEditText != null) {
                    passwordEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No-op.
            }
        };

        if (emailEditText != null) {
            emailEditText.addTextChangedListener(clearErrorWatcher);
        }
        if (passwordEditText != null) {
            passwordEditText.addTextChangedListener(clearErrorWatcher);
        }
    }

    private void submitLogin() {
        if (isLoading || authRepository == null) {
            return;
        }

        String usernameOrEmail = getTrimmedText(emailEditText);
        String password = getTrimmedText(passwordEditText);

        if (usernameOrEmail.isEmpty()) {
            if (emailEditText != null) {
                emailEditText.setError(getString(R.string.login_validation_email_required));
                emailEditText.requestFocus();
            }
            return;
        }

        if (password.isEmpty()) {
            if (passwordEditText != null) {
                passwordEditText.setError(getString(R.string.login_validation_password_required));
                passwordEditText.requestFocus();
            }
            return;
        }

        setLoading(true);
        authRepository.login(usernameOrEmail, password, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                navigateToHomeActivityByRole();
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setLoading(false);
                Toast.makeText(
                        LoginActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        if (emailEditText != null) {
            emailEditText.setEnabled(!loading);
        }
        if (passwordEditText != null) {
            passwordEditText.setEnabled(!loading);
        }
        if (loginButton != null) {
            loginButton.setEnabled(!loading);
            loginButton.setText(loading ? getString(R.string.login_loading) : defaultLoginButtonText);
            loginButton.setAlpha(loading ? 0.8f : 1f);
        }
    }

    private void navigateToHomeActivityByRole() {
        Class<?> destination = NavActivity.class;
        if (authRepository != null && authRepository.isCurrentRoleCaptain()) {
            destination = CaptainNavigationActivity.class;
        }
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @NonNull
    private String getTrimmedText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
