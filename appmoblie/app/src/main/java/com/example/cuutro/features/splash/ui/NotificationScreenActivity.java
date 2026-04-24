package com.example.cuutro.features.splash.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cuutro.R;
import com.example.cuutro.features.auth.ui.LoginActivity;

public class NotificationScreenActivity extends AppCompatActivity
        implements NotificationScreenFragment.ActionListener {

    private static final String EXTRA_SCREEN_STATE = "extra_screen_state";
    private static final String EXTRA_OVERRIDE_TITLE = "extra_override_title";
    private static final String EXTRA_OVERRIDE_DESCRIPTION = "extra_override_description";
    private static final String EXTRA_OVERRIDE_BUTTON = "extra_override_button";
    private static final String EXTRA_OVERRIDE_FOOTER = "extra_override_footer";
    private static final String EXTRA_OVERRIDE_ICON = "extra_override_icon";
    private static final String EXTRA_OVERRIDE_ACTION = "extra_override_action";

    @NonNull
    public static Intent createIntent(
            @NonNull Context context,
            @NonNull NotificationScreenFragment.ScreenState state
    ) {
        Intent intent = new Intent(context, NotificationScreenActivity.class);
        intent.putExtra(EXTRA_SCREEN_STATE, state.name());
        return intent;
    }

    @NonNull
    public static Intent createUnauthorizedIntent(
            @NonNull Context context,
            @Nullable String description
    ) {
        Intent intent = createIntent(context, NotificationScreenFragment.ScreenState.UNAUTHORIZED);
        if (!TextUtils.isEmpty(description)) {
            intent.putExtra(EXTRA_OVERRIDE_DESCRIPTION, description);
        }
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_screen);

        if (savedInstanceState != null) {
            return;
        }

        NotificationScreenFragment.ScreenState state = parseState(
                getIntent() != null ? getIntent().getStringExtra(EXTRA_SCREEN_STATE) : null
        );
        NotificationScreenFragment.ScreenOverride screenOverride = parseOverrideFromIntent(getIntent());

        NotificationScreenFragment fragment = NotificationScreenFragment.newInstance(state, screenOverride);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.notification_screen_container, fragment)
                .commit();
    }

    @Override
    public void onNotificationAction(
            @NonNull NotificationScreenFragment.ActionType actionType,
            @NonNull NotificationScreenFragment.ScreenState state
    ) {
        if (actionType == NotificationScreenFragment.ActionType.LOGIN) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(NotificationScreenFragment.RESULT_ACTION_KEY, actionType.name());
        resultIntent.putExtra(NotificationScreenFragment.RESULT_STATE_KEY, state.name());
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @NonNull
    private NotificationScreenFragment.ScreenState parseState(@Nullable String rawState) {
        if (TextUtils.isEmpty(rawState)) {
            return NotificationScreenFragment.ScreenState.ERROR;
        }
        try {
            return NotificationScreenFragment.ScreenState.valueOf(rawState);
        } catch (IllegalArgumentException ignored) {
            return NotificationScreenFragment.ScreenState.ERROR;
        }
    }

    @Nullable
    private NotificationScreenFragment.ScreenOverride parseOverrideFromIntent(@Nullable Intent intent) {
        if (intent == null) {
            return null;
        }

        String title = intent.getStringExtra(EXTRA_OVERRIDE_TITLE);
        String description = intent.getStringExtra(EXTRA_OVERRIDE_DESCRIPTION);
        String button = intent.getStringExtra(EXTRA_OVERRIDE_BUTTON);
        String footer = intent.getStringExtra(EXTRA_OVERRIDE_FOOTER);
        int iconResId = intent.getIntExtra(EXTRA_OVERRIDE_ICON, 0);
        String rawAction = intent.getStringExtra(EXTRA_OVERRIDE_ACTION);

        boolean hasOverride = !TextUtils.isEmpty(title)
                || !TextUtils.isEmpty(description)
                || !TextUtils.isEmpty(button)
                || !TextUtils.isEmpty(footer)
                || iconResId != 0
                || !TextUtils.isEmpty(rawAction);
        if (!hasOverride) {
            return null;
        }

        NotificationScreenFragment.ScreenOverride screenOverride = new NotificationScreenFragment.ScreenOverride();
        if (!TextUtils.isEmpty(title)) {
            screenOverride.setTitle(title);
        }
        if (!TextUtils.isEmpty(description)) {
            screenOverride.setDescription(description);
        }
        if (!TextUtils.isEmpty(button)) {
            screenOverride.setButton(button);
        }
        if (!TextUtils.isEmpty(footer)) {
            screenOverride.setFooter(footer);
        }
        if (iconResId != 0) {
            screenOverride.setIconResId(iconResId);
        }
        NotificationScreenFragment.ActionType actionType = parseActionType(rawAction);
        if (actionType != null) {
            screenOverride.setActionType(actionType);
        }

        return screenOverride;
    }

    @Nullable
    private NotificationScreenFragment.ActionType parseActionType(@Nullable String rawAction) {
        if (TextUtils.isEmpty(rawAction)) {
            return null;
        }
        try {
            return NotificationScreenFragment.ActionType.valueOf(rawAction);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
