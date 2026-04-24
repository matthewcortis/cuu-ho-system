package com.example.cuutro.features.profile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.auth.ui.LoginActivity;
import com.example.cuutro.features.setting.ui.SettingActivity;
import com.example.cuutro.features.splash.ui.NotificationScreenActivity;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    private AuthRepository authRepository;
    private MaterialButton authActionButton;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MyApp app = (MyApp) requireActivity().getApplication();
        authRepository = app.getAppContainer().getAuthRepository();

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
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAuthActionButton();
    }

    private void updateAuthActionButton() {
        if (authActionButton == null) {
            return;
        }
        authActionButton.setText(isUserLoggedIn() ? R.string.profile_logout : R.string.profile_login);
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

    private boolean isUserLoggedIn() {
        return authRepository != null && authRepository.hasActiveSession();
    }
}
