package com.example.cuutro.features.setting.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.core.ui.AppThemeManager;
import com.example.cuutro.features.profile.data.ProfileRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class SettingActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private SwitchCompat darkModeSwitch;
    private ProfileRepository profileRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        darkModeSwitch = findViewById(R.id.switch_dark_mode);
        initDependencies();
        setupActions();
        bindThemeState();
    }

    private void initDependencies() {
        if (!(getApplication() instanceof MyApp)) {
            return;
        }
        MyApp myApp = (MyApp) getApplication();
        if (myApp.getAppContainer() == null) {
            return;
        }
        profileRepository = myApp.getAppContainer().getProfileRepository();
    }

    private void setupActions() {
        View backButton = findViewById(R.id.btn_setting_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        View changePasswordRow = findViewById(R.id.row_setting_change_password);
        if (changePasswordRow != null) {
            changePasswordRow.setOnClickListener(v -> showChangePasswordDialog());
        }
    }

    private void bindThemeState() {
        if (darkModeSwitch == null) {
            return;
        }
        darkModeSwitch.setOnCheckedChangeListener(null);
        darkModeSwitch.setChecked(AppThemeManager.isDarkModeEnabled(this));
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppThemeManager.setDarkModeEnabled(SettingActivity.this, isChecked)
        );
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null, false);
        TextInputLayout currentPasswordInputLayout = dialogView.findViewById(R.id.input_layout_current_password);
        TextInputLayout newPasswordInputLayout = dialogView.findViewById(R.id.input_layout_new_password);
        TextInputLayout confirmPasswordInputLayout = dialogView.findViewById(R.id.input_layout_confirm_password);
        TextInputEditText currentPasswordEditText = dialogView.findViewById(R.id.edt_current_password);
        TextInputEditText newPasswordEditText = dialogView.findViewById(R.id.edt_new_password);
        TextInputEditText confirmPasswordEditText = dialogView.findViewById(R.id.edt_confirm_password);
        MaterialButton cancelButton = dialogView.findViewById(R.id.btn_change_password_cancel);
        MaterialButton confirmButton = dialogView.findViewById(R.id.btn_change_password_confirm);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }
        if (confirmButton != null) {
            String defaultConfirmButtonText = confirmButton.getText() != null
                    ? confirmButton.getText().toString()
                    : getString(R.string.setting_confirm);
            confirmButton.setOnClickListener(v -> {
                boolean isValid = validateChangePasswordInputs(
                        currentPasswordInputLayout,
                        newPasswordInputLayout,
                        confirmPasswordInputLayout,
                        currentPasswordEditText,
                        newPasswordEditText,
                        confirmPasswordEditText
                );
                if (!isValid) {
                    return;
                }
                if (profileRepository == null) {
                    Toast.makeText(
                            SettingActivity.this,
                            getString(R.string.setting_change_password_failed, "Thiếu cấu hình dữ liệu người dùng"),
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }
                String currentPassword = getInputText(currentPasswordEditText);
                String newPassword = getInputText(newPasswordEditText);
                setChangePasswordDialogLoading(
                        true,
                        dialog,
                        confirmButton,
                        cancelButton,
                        currentPasswordEditText,
                        newPasswordEditText,
                        confirmPasswordEditText,
                        defaultConfirmButtonText
                );
                profileRepository.changeCurrentUserPassword(currentPassword, newPassword, new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        setChangePasswordDialogLoading(
                                false,
                                dialog,
                                confirmButton,
                                cancelButton,
                                currentPasswordEditText,
                                newPasswordEditText,
                                confirmPasswordEditText,
                                defaultConfirmButtonText
                        );
                        dialog.dismiss();
                        Toast.makeText(
                                SettingActivity.this,
                                R.string.setting_change_password_success,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        setChangePasswordDialogLoading(
                                false,
                                dialog,
                                confirmButton,
                                cancelButton,
                                currentPasswordEditText,
                                newPasswordEditText,
                                confirmPasswordEditText,
                                defaultConfirmButtonText
                        );
                        if (shouldShowCurrentPasswordError(error)) {
                            currentPasswordInputLayout.setError(error.getMessage());
                            currentPasswordEditText.requestFocus();
                            return;
                        }
                        Toast.makeText(
                                SettingActivity.this,
                                getString(R.string.setting_change_password_failed, error.getMessage()),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            });
        }

        dialog.show();
    }

    private boolean validateChangePasswordInputs(
            @NonNull TextInputLayout currentPasswordInputLayout,
            @NonNull TextInputLayout newPasswordInputLayout,
            @NonNull TextInputLayout confirmPasswordInputLayout,
            @NonNull TextInputEditText currentPasswordEditText,
            @NonNull TextInputEditText newPasswordEditText,
            @NonNull TextInputEditText confirmPasswordEditText
    ) {
        currentPasswordInputLayout.setError(null);
        newPasswordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);

        String currentPassword = getInputText(currentPasswordEditText);
        String newPassword = getInputText(newPasswordEditText);
        String confirmPassword = getInputText(confirmPasswordEditText);
        boolean isValid = true;

        if (TextUtils.isEmpty(currentPassword.trim())) {
            currentPasswordInputLayout.setError(getString(R.string.setting_current_password_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(newPassword.trim())) {
            newPasswordInputLayout.setError(getString(R.string.setting_new_password_required));
            isValid = false;
        } else if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            newPasswordInputLayout.setError(getString(R.string.setting_new_password_min_length));
            isValid = false;
        } else if (newPassword.equals(currentPassword)) {
            newPasswordInputLayout.setError(getString(R.string.setting_new_password_must_different));
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword.trim())) {
            confirmPasswordInputLayout.setError(getString(R.string.setting_confirm_password_required));
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
            confirmPasswordInputLayout.setError(getString(R.string.setting_confirm_password_not_match));
            isValid = false;
        }

        return isValid;
    }

    private boolean shouldShowCurrentPasswordError(@NonNull NetworkError error) {
        if (error.getStatusCode() != 400) {
            return false;
        }
        String normalizedMessage = error.getMessage() == null
                ? ""
                : error.getMessage().trim().toLowerCase(Locale.ROOT);
        return normalizedMessage.contains("hien tai")
                || normalizedMessage.contains("current password");
    }

    private void setChangePasswordDialogLoading(
            boolean loading,
            @NonNull AlertDialog dialog,
            @NonNull MaterialButton confirmButton,
            @Nullable MaterialButton cancelButton,
            @NonNull TextInputEditText currentPasswordEditText,
            @NonNull TextInputEditText newPasswordEditText,
            @NonNull TextInputEditText confirmPasswordEditText,
            @NonNull String defaultConfirmButtonText
    ) {
        dialog.setCancelable(!loading);
        dialog.setCanceledOnTouchOutside(!loading);

        currentPasswordEditText.setEnabled(!loading);
        newPasswordEditText.setEnabled(!loading);
        confirmPasswordEditText.setEnabled(!loading);

        confirmButton.setEnabled(!loading);
        confirmButton.setText(loading ? getString(R.string.setting_change_password_loading) : defaultConfirmButtonText);
        confirmButton.setAlpha(loading ? 0.85f : 1f);
        if (cancelButton != null) {
            cancelButton.setEnabled(!loading);
            cancelButton.setAlpha(loading ? 0.85f : 1f);
        }
    }

    @NonNull
    private String getInputText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString();
    }
}
