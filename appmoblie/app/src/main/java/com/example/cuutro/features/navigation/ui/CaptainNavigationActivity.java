package com.example.cuutro.features.navigation.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.auth.ui.LoginActivity;
import com.example.cuutro.features.captain.ui.CaptainMembersFragment;
import com.example.cuutro.features.profile.ui.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CaptainNavigationActivity extends AppCompatActivity {

    private static final String TAG_MEMBER_MESSAGES = "captain_tab_member_messages";
    private static final String TAG_MEMBERS = "captain_tab_members";
    private static final String TAG_ME = "captain_tab_me";

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, CaptainNavigationActivity.class);
    }

    @Nullable
    private AuthRepository authRepository;
    private BottomNavigationView bottomNavigationView;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captain_navigation);

        MyApp app = (MyApp) getApplication();
        if (app.getAppContainer() != null) {
            authRepository = app.getAppContainer().getAuthRepository();
        }

        if (!ensureCaptainAuthorized()) {
            return;
        }

        bottomNavigationView = findViewById(R.id.bottom_nav_captain);
        restoreActiveFragment();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switchToTab(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_captain_member_messages);
        } else if (activeFragment == null) {
            switchToTab(bottomNavigationView.getSelectedItemId());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ensureCaptainAuthorized();
    }

    private boolean ensureCaptainAuthorized() {
        if (authRepository == null || !authRepository.hasActiveSession()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return false;
        }
        if (!authRepository.isCurrentRoleCaptain()) {
            Intent intent = new Intent(this, NavActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return false;
        }
        return true;
    }

    private void switchToTab(int itemId) {
        String tag = getTagForItem(itemId);
        if (tag == null) {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment targetFragment = fragmentManager.findFragmentByTag(tag);
        if (targetFragment == null) {
            targetFragment = createFragment(itemId);
        }
        if (targetFragment == null) {
            return;
        }

        if (activeFragment == targetFragment && targetFragment.isVisible()) {
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction().setReorderingAllowed(true);
        if (!targetFragment.isAdded()) {
            transaction.add(R.id.captain_fragment_container, targetFragment, tag);
        }
        if (activeFragment != null && activeFragment != targetFragment && activeFragment.isAdded()) {
            transaction.hide(activeFragment);
        }
        transaction.show(targetFragment);
        transaction.commit();
        activeFragment = targetFragment;
    }

    private void restoreActiveFragment() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null && fragment.isVisible()) {
                activeFragment = fragment;
                return;
            }
        }
    }

    @Nullable
    private String getTagForItem(int itemId) {
        if (itemId == R.id.nav_captain_member_messages) {
            return TAG_MEMBER_MESSAGES;
        }
        if (itemId == R.id.nav_captain_members) {
            return TAG_MEMBERS;
        }
        if (itemId == R.id.nav_captain_me) {
            return TAG_ME;
        }
        return null;
    }

    @Nullable
    private Fragment createFragment(int itemId) {
        if (itemId == R.id.nav_captain_member_messages) {
            return new CaptainMemberMessagesFragment();
        }
        if (itemId == R.id.nav_captain_members) {
            return new CaptainMembersFragment();
        }
        if (itemId == R.id.nav_captain_me) {
            return new ProfileFragment();
        }
        return null;
    }
}
