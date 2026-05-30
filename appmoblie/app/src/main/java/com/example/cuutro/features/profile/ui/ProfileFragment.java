package com.example.cuutro.features.profile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.auth.ui.LoginActivity;
import com.example.cuutro.features.profile.data.ProfileRepository;
import com.example.cuutro.features.profile.data.model.UserProfileData;
import com.example.cuutro.features.setting.ui.SettingActivity;
import com.example.cuutro.features.splash.ui.NotificationScreenActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.regex.Pattern;

public class ProfileFragment extends Fragment {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

    private AuthRepository authRepository;
    private ProfileRepository profileRepository;
    private MaterialButton authActionButton;
    private View registerVolunteerButton;
    private boolean isRegisteringVolunteer;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MyApp app = (MyApp) requireActivity().getApplication();
        authRepository = app.getAppContainer().getAuthRepository();
        profileRepository = app.getAppContainer().getProfileRepository();

        View accountDetailsButton = view.findViewById(R.id.chi_tiet_tai_khoan_btn);
        if (accountDetailsButton != null) {
            accountDetailsButton.setOnClickListener(v -> {
                if (!isUserLoggedIn()) {
                    startActivity(
                            NotificationScreenActivity.createUnauthorizedIntent(
                                    requireContext(),
                                    getString(R.string.auth_required_account_details_message)
                            )
                    );
                    return;
                }
                startActivity(new Intent(requireContext(), AccountDetailsActivity.class));
            });
        }

        registerVolunteerButton = view.findViewById(R.id.btn_profile_register_volunteer);
        if (registerVolunteerButton != null) {
            registerVolunteerButton.setOnClickListener(v -> handleVolunteerRegistration());
        }

        View logoutButton = view.findViewById(R.id.btn_profile_logout);
        if (logoutButton instanceof MaterialButton) {
            authActionButton = (MaterialButton) logoutButton;
            authActionButton.setOnClickListener(v -> handleAuthAction());
        }

        View settingButton = view.findViewById(R.id.btn_profile_settings);
        if (settingButton != null) {
            settingButton.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), SettingActivity.class))
            );
        }

        updateAuthActionButton();
        updateVolunteerRegisterButtonState();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAuthActionButton();
        updateVolunteerRegisterButtonState();
    }

    private void updateAuthActionButton() {
        if (authActionButton == null) {
            return;
        }
        authActionButton.setText(isUserLoggedIn() ? R.string.profile_logout : R.string.profile_login);
    }

    private void updateVolunteerRegisterButtonState() {
        if (registerVolunteerButton == null) {
            return;
        }
        boolean shouldShow = isUserLoggedIn() && !isCurrentUserVolunteer();
        registerVolunteerButton.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        registerVolunteerButton.setEnabled(shouldShow && !isRegisteringVolunteer);
        registerVolunteerButton.setAlpha(isRegisteringVolunteer ? 0.75f : 1f);
    }

    private void handleAuthAction() {
        if (isUserLoggedIn()) {
            handleLogout();
            return;
        }
        navigateToLogin();
    }

    private void navigateToLogin() {
        startActivity(new Intent(requireContext(), LoginActivity.class));
    }

    private void handleLogout() {
        if (authRepository != null) {
            authRepository.clearSession();
        }
        Toast.makeText(requireContext(), R.string.profile_logged_out_message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void handleVolunteerRegistration() {
        if (!isUserLoggedIn()) {
            startActivity(
                    NotificationScreenActivity.createUnauthorizedIntent(
                            requireContext(),
                            getString(R.string.auth_required_account_details_message)
                    )
            );
            return;
        }
        if (isCurrentUserVolunteer()) {
            Toast.makeText(
                    requireContext(),
                    R.string.profile_register_volunteer_already_member,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        if (profileRepository == null || isRegisteringVolunteer) {
            return;
        }

        showVolunteerRegistrationDialog();
    }

    private void showVolunteerRegistrationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_register_volunteer, null, false);
        TextInputEditText canHelpEditText = dialogView.findViewById(R.id.edt_volunteer_can_help);
        TextInputEditText noteEditText = dialogView.findViewById(R.id.edt_volunteer_note);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .setNegativeButton(R.string.setting_cancel, null)
                .setPositiveButton(R.string.profile_register_volunteer_dialog_confirm, null)
                .create();
        dialog.setOnShowListener(ignored -> {
            Button confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (confirmButton == null) {
                return;
            }
            confirmButton.setOnClickListener(v -> {
                String coTheGiup = trimToNull(getInputText(canHelpEditText));
                String ghiChu = trimToNull(getInputText(noteEditText));
                dialog.dismiss();
                submitVolunteerRegistration(coTheGiup, ghiChu);
            });
        });
        dialog.show();
    }

    private void submitVolunteerRegistration(
            @Nullable String coTheGiup,
            @Nullable String ghiChu
    ) {
        setVolunteerRegisterLoading(true);
        profileRepository.getCurrentUserProfile(new ResultCallback<UserProfileData>() {
            @Override
            public void onSuccess(UserProfileData data) {
                if (!isAdded()) {
                    return;
                }
                if (!isProfileCompleteForVolunteer(data)) {
                    setVolunteerRegisterLoading(false);
                    Toast.makeText(
                            requireContext(),
                            R.string.profile_register_volunteer_profile_incomplete,
                            Toast.LENGTH_LONG
                    ).show();
                    startActivity(new Intent(requireContext(), AccountDetailsActivity.class));
                    return;
                }

                profileRepository.registerCurrentUserAsVolunteer(data, coTheGiup, ghiChu, new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void ignored) {
                        if (!isAdded()) {
                            return;
                        }
                        setVolunteerRegisterLoading(false);
                        Toast.makeText(
                                requireContext(),
                                R.string.profile_register_volunteer_success,
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (!isAdded()) {
                            return;
                        }
                        setVolunteerRegisterLoading(false);
                        if (error.isUnauthorized()) {
                            forceRelogin(R.string.auth_session_expired);
                            return;
                        }
                        if (error.getStatusCode() == 403) {
                            forceRelogin(R.string.auth_role_not_supported);
                            return;
                        }
                        Toast.makeText(
                                requireContext(),
                                getString(R.string.profile_register_volunteer_failed, error.getMessage()),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (!isAdded()) {
                    return;
                }
                setVolunteerRegisterLoading(false);
                if (error.isUnauthorized()) {
                    forceRelogin(R.string.auth_session_expired);
                    return;
                }
                if (error.getStatusCode() == 403) {
                    forceRelogin(R.string.auth_role_not_supported);
                    return;
                }
                Toast.makeText(
                        requireContext(),
                        getString(R.string.profile_register_volunteer_profile_load_failed, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @Nullable
    private String getInputText(@Nullable TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return null;
        }
        return editText.getText().toString();
    }

    private void setVolunteerRegisterLoading(boolean loading) {
        isRegisteringVolunteer = loading;
        updateVolunteerRegisterButtonState();
    }

    private void forceRelogin(int messageResId) {
        if (authRepository != null) {
            authRepository.clearSession();
        }
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_LONG).show();
        navigateToLogin();
    }

    private boolean isProfileCompleteForVolunteer(@Nullable UserProfileData profile) {
        if (profile == null) {
            return false;
        }
        String hoTen = trimToNull(profile.getHoTen());
        String email = trimToNull(profile.getEmail());
        String soDienThoai = normalizePhoneDigits(profile.getSoDienThoai());
        String diaChi = trimToNull(profile.getDiaChi());
        if (hoTen == null || email == null || soDienThoai == null || diaChi == null) {
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        }
        return PHONE_PATTERN.matcher(soDienThoai).matches();
    }

    @Nullable
    private String normalizePhoneDigits(@Nullable String rawPhone) {
        if (rawPhone == null) {
            return null;
        }
        String digits = rawPhone.replaceAll("\\D+", "");
        return digits.isEmpty() ? null : digits;
    }

    @Nullable
    private String trimToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isCurrentUserVolunteer() {
        return authRepository != null && authRepository.isCurrentRoleCaptain();
    }

    private boolean isUserLoggedIn() {
        return authRepository != null && authRepository.hasActiveSession();
    }
}
