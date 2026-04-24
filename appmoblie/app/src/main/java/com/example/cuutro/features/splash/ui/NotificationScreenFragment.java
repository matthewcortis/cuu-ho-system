package com.example.cuutro.features.splash.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cuutro.R;
import com.google.android.material.button.MaterialButton;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class NotificationScreenFragment extends Fragment {

    public enum ScreenState {
        NO_INTERNET,
        UNAUTHORIZED,
        ERROR,
        NOT_FOUND,
        EMPTY
    }

    public enum ActionType {
        RETRY,
        LOGIN,
        DISMISS,
        NONE
    }

    public interface ActionListener {
        void onNotificationAction(@NonNull ActionType actionType, @NonNull ScreenState state);
    }

    private static final class StateConfig {
        final int iconResId;
        final int titleResId;
        final int descriptionResId;
        final int buttonResId;
        final int footerResId;
        final ActionType actionType;

        StateConfig(
                int iconResId,
                int titleResId,
                int descriptionResId,
                int buttonResId,
                int footerResId,
                @NonNull ActionType actionType
        ) {
            this.iconResId = iconResId;
            this.titleResId = titleResId;
            this.descriptionResId = descriptionResId;
            this.buttonResId = buttonResId;
            this.footerResId = footerResId;
            this.actionType = actionType;
        }
    }

    public static class ScreenOverride {
        private String title;
        private String description;
        private String button;
        private String footer;
        private Integer iconResId;
        private ActionType actionType;

        @NonNull
        public ScreenOverride setTitle(@Nullable String title) {
            this.title = title;
            return this;
        }

        @NonNull
        public ScreenOverride setDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        @NonNull
        public ScreenOverride setButton(@Nullable String button) {
            this.button = button;
            return this;
        }

        @NonNull
        public ScreenOverride setFooter(@Nullable String footer) {
            this.footer = footer;
            return this;
        }

        @NonNull
        public ScreenOverride setIconResId(int iconResId) {
            this.iconResId = iconResId;
            return this;
        }

        @NonNull
        public ScreenOverride setActionType(@Nullable ActionType actionType) {
            this.actionType = actionType;
            return this;
        }
    }

    public static final String DEFAULT_RESULT_REQUEST_KEY = "notification_screen_result";
    public static final String RESULT_ACTION_KEY = "result_action_key";
    public static final String RESULT_STATE_KEY = "result_state_key";

    private static final String ARG_SCREEN_STATE = "arg_screen_state";
    private static final String ARG_RESULT_REQUEST_KEY = "arg_result_request_key";
    private static final String ARG_OVERRIDE_TITLE = "arg_override_title";
    private static final String ARG_OVERRIDE_DESCRIPTION = "arg_override_description";
    private static final String ARG_OVERRIDE_BUTTON = "arg_override_button";
    private static final String ARG_OVERRIDE_FOOTER = "arg_override_footer";
    private static final String ARG_OVERRIDE_ICON = "arg_override_icon";
    private static final String ARG_OVERRIDE_ACTION = "arg_override_action";

    private static final Map<ScreenState, StateConfig> STATE_CONFIG;

    static {
        EnumMap<ScreenState, StateConfig> map = new EnumMap<>(ScreenState.class);
        map.put(ScreenState.NO_INTERNET, new StateConfig(
                R.drawable.ic_report_search,
                R.string.notification_no_internet_title,
                R.string.notification_no_internet_message,
                R.string.notification_retry_action,
                R.string.notification_no_internet_footer,
                ActionType.RETRY
        ));
        map.put(ScreenState.UNAUTHORIZED, new StateConfig(
                R.drawable.ic_signup_lock,
                R.string.notification_unauthorized_title,
                R.string.notification_unauthorized_message,
                R.string.notification_login_action,
                R.string.notification_unauthorized_footer,
                ActionType.LOGIN
        ));
        map.put(ScreenState.ERROR, new StateConfig(
                R.drawable.ic_emergency_other,
                R.string.notification_error_title,
                R.string.notification_error_message,
                R.string.notification_retry_action,
                R.string.notification_error_footer,
                ActionType.RETRY
        ));
        map.put(ScreenState.NOT_FOUND, new StateConfig(
                R.drawable.ic_report_search,
                R.string.notification_not_found_title,
                R.string.notification_not_found_message,
                R.string.notification_dismiss_action,
                R.string.notification_not_found_footer,
                ActionType.DISMISS
        ));
        map.put(ScreenState.EMPTY, new StateConfig(
                R.drawable.ic_report_search,
                R.string.notification_empty_title,
                R.string.notification_empty_message,
                R.string.notification_dismiss_action,
                R.string.notification_empty_footer,
                ActionType.DISMISS
        ));
        STATE_CONFIG = Collections.unmodifiableMap(map);
    }

    public NotificationScreenFragment() {
        super(R.layout.fragment_notification_screen);
    }

    @NonNull
    public static NotificationScreenFragment newNeedLoginScreen() {
        return newInstance(ScreenState.UNAUTHORIZED);
    }

    @NonNull
    public static NotificationScreenFragment newNoInternetScreen() {
        return newInstance(ScreenState.NO_INTERNET, null, DEFAULT_RESULT_REQUEST_KEY);
    }

    @NonNull
    public static NotificationScreenFragment newNoInternetScreen(@Nullable String resultRequestKey) {
        String requestKey = TextUtils.isEmpty(resultRequestKey) ? DEFAULT_RESULT_REQUEST_KEY : resultRequestKey;
        return newInstance(ScreenState.NO_INTERNET, null, requestKey);
    }

    @NonNull
    public static NotificationScreenFragment newInstance(@NonNull ScreenState state) {
        return newInstance(state, null, null);
    }

    @NonNull
    public static NotificationScreenFragment newInstance(
            @NonNull ScreenState state,
            @Nullable ScreenOverride screenOverride
    ) {
        return newInstance(state, screenOverride, null);
    }

    @NonNull
    public static NotificationScreenFragment newInstance(
            @NonNull ScreenState state,
            @Nullable ScreenOverride screenOverride,
            @Nullable String resultRequestKey
    ) {
        NotificationScreenFragment fragment = new NotificationScreenFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SCREEN_STATE, state.name());

        if (screenOverride != null) {
            if (!TextUtils.isEmpty(screenOverride.title)) {
                args.putString(ARG_OVERRIDE_TITLE, screenOverride.title);
            }
            if (!TextUtils.isEmpty(screenOverride.description)) {
                args.putString(ARG_OVERRIDE_DESCRIPTION, screenOverride.description);
            }
            if (!TextUtils.isEmpty(screenOverride.button)) {
                args.putString(ARG_OVERRIDE_BUTTON, screenOverride.button);
            }
            if (!TextUtils.isEmpty(screenOverride.footer)) {
                args.putString(ARG_OVERRIDE_FOOTER, screenOverride.footer);
            }
            if (screenOverride.iconResId != null) {
                args.putInt(ARG_OVERRIDE_ICON, screenOverride.iconResId);
            }
            if (screenOverride.actionType != null) {
                args.putString(ARG_OVERRIDE_ACTION, screenOverride.actionType.name());
            }
        }

        if (!TextUtils.isEmpty(resultRequestKey)) {
            args.putString(ARG_RESULT_REQUEST_KEY, resultRequestKey);
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView iconView = view.findViewById(R.id.iv_notification_icon);
        TextView titleView = view.findViewById(R.id.tv_notification_title);
        TextView messageView = view.findViewById(R.id.tv_notification_message);
        TextView footerView = view.findViewById(R.id.tv_notification_footer);
        MaterialButton primaryButton = view.findViewById(R.id.btn_notification_primary_action);

        if (titleView == null || messageView == null || footerView == null || primaryButton == null) {
            return;
        }

        Bundle args = getArguments() == null ? Bundle.EMPTY : getArguments();
        ScreenState state = parseScreenState(args.getString(ARG_SCREEN_STATE));
        StateConfig config = STATE_CONFIG.get(state);
        if (config == null) {
            config = STATE_CONFIG.get(ScreenState.ERROR);
        }
        if (config == null) {
            return;
        }

        int iconResId = args.getInt(ARG_OVERRIDE_ICON, config.iconResId);
        if (iconView != null && iconResId != 0) {
            iconView.setImageResource(iconResId);
        }

        bindTextOrResource(titleView, args.getString(ARG_OVERRIDE_TITLE), config.titleResId);
        bindTextOrResource(messageView, args.getString(ARG_OVERRIDE_DESCRIPTION), config.descriptionResId);
        bindFooterText(footerView, args.getString(ARG_OVERRIDE_FOOTER), config.footerResId);

        ActionType actionType = parseActionType(args.getString(ARG_OVERRIDE_ACTION), config.actionType);
        bindActionButton(
                primaryButton,
                actionType,
                args.getString(ARG_OVERRIDE_BUTTON),
                config.buttonResId,
                state,
                args
        );
    }

    private void bindActionButton(
            @NonNull MaterialButton button,
            @NonNull ActionType actionType,
            @Nullable String buttonTextOverride,
            int defaultTextResId,
            @NonNull ScreenState state,
            @NonNull Bundle args
    ) {
        if (actionType == ActionType.NONE) {
            button.setVisibility(View.GONE);
            button.setOnClickListener(null);
            return;
        }

        button.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(buttonTextOverride)) {
            button.setText(buttonTextOverride);
        } else if (defaultTextResId != 0) {
            button.setText(defaultTextResId);
        }
        button.setOnClickListener(v -> dispatchActionToHost(actionType, state, args));
    }

    private void bindFooterText(@NonNull TextView footerView, @Nullable String footerOverride, int footerResId) {
        if (!TextUtils.isEmpty(footerOverride)) {
            footerView.setVisibility(View.VISIBLE);
            footerView.setText(footerOverride);
            return;
        }
        if (footerResId != 0) {
            footerView.setVisibility(View.VISIBLE);
            footerView.setText(footerResId);
            return;
        }
        footerView.setVisibility(View.GONE);
    }

    private void bindTextOrResource(@NonNull TextView textView, @Nullable String overrideValue, int textResId) {
        if (!TextUtils.isEmpty(overrideValue)) {
            textView.setText(overrideValue);
            return;
        }
        if (textResId != 0) {
            textView.setText(textResId);
        } else {
            textView.setText("");
        }
    }

    private void dispatchActionToHost(
            @NonNull ActionType actionType,
            @NonNull ScreenState state,
            @NonNull Bundle args
    ) {
        Fragment parent = getParentFragment();
        if (parent instanceof ActionListener) {
            ((ActionListener) parent).onNotificationAction(actionType, state);
            return;
        }
        if (getActivity() instanceof ActionListener) {
            ((ActionListener) getActivity()).onNotificationAction(actionType, state);
            return;
        }

        String resultRequestKey = args.getString(ARG_RESULT_REQUEST_KEY);
        if (!TextUtils.isEmpty(resultRequestKey)) {
            Bundle result = new Bundle();
            result.putString(RESULT_ACTION_KEY, actionType.name());
            result.putString(RESULT_STATE_KEY, state.name());
            getParentFragmentManager().setFragmentResult(resultRequestKey, result);
        }
    }

    @NonNull
    private ScreenState parseScreenState(@Nullable String value) {
        if (TextUtils.isEmpty(value)) {
            return ScreenState.ERROR;
        }
        try {
            return ScreenState.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return ScreenState.ERROR;
        }
    }

    @NonNull
    private ActionType parseActionType(@Nullable String value, @NonNull ActionType fallbackAction) {
        if (TextUtils.isEmpty(value)) {
            return fallbackAction;
        }
        try {
            return ActionType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return fallbackAction;
        }
    }
}
