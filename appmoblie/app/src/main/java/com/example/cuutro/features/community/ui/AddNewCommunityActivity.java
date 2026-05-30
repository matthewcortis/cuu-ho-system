package com.example.cuutro.features.community.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.community.data.CommunityRepository;
import com.example.cuutro.features.splash.ui.NotificationScreenActivity;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class AddNewCommunityActivity extends AppCompatActivity {

    private AuthRepository authRepository;
    private CommunityRepository communityRepository;

    private ImageButton backButton;
    private MaterialButton submitButton;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private TextView locationValueView;
    private TextView selectedMediaView;
    private View uploadMediaLayout;

    private boolean isSubmitting;
    private Uri selectedMediaUri;
    private final ActivityResultLauncher<String> pickMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::handleMediaPicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_new_community);

        MyApp app = (MyApp) getApplication();
        authRepository = app.getAppContainer().getAuthRepository();
        communityRepository = app.getAppContainer().getCommunityRepository();
        if (authRepository == null || !authRepository.hasActiveSession()) {
            startActivity(
                    NotificationScreenActivity.createUnauthorizedIntent(
                            this,
                            getString(R.string.auth_required_create_post_message)
                    )
            );
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        setupActions();
    }

    private void bindViews() {
        backButton = findViewById(R.id.btnBackCreatePost);
        submitButton = findViewById(R.id.btnSubmitCommunityPost);
        titleEditText = findViewById(R.id.etCommunityPostTitle);
        descriptionEditText = findViewById(R.id.etCommunityPostDescription);
        locationValueView = findViewById(R.id.tvCommunityPostLocationValue);
        selectedMediaView = findViewById(R.id.tvCommunityPostSelectedMedia);
        uploadMediaLayout = findViewById(R.id.layoutUploadMedia);
    }

    private void setupActions() {
        TextView changeLocationView = findViewById(R.id.tvCommunityPostChangeLocation);

        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (!isSubmitting) {
                    finish();
                }
            });
        }
        if (uploadMediaLayout != null) {
            uploadMediaLayout.setOnClickListener(v -> {
                if (!isSubmitting) {
                    pickMediaLauncher.launch("*/*");
                }
            });
        }
        changeLocationView.setOnClickListener(v -> showLocationEditorDialog());
        if (submitButton != null) {
            submitButton.setOnClickListener(v -> submitPost());
        }
    }

    private void showLocationEditorDialog() {
        if (locationValueView == null || isFinishing()) {
            return;
        }
        EditText input = new EditText(this);
        String currentLocation = trimToNull(getText(locationValueView));
        String placeholder = getString(R.string.community_add_post_location_placeholder);
        if (!TextUtils.equals(currentLocation, placeholder)) {
            input.setText(currentLocation);
        }
        input.setHint(R.string.community_add_post_location_hint);

        new AlertDialog.Builder(this)
                .setTitle(R.string.community_add_post_location_dialog_title)
                .setView(input)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.community_add_post_location_apply, (dialog, which) -> {
                    String selectedLocation = trimToNull(getText(input));
                    locationValueView.setText(
                            selectedLocation == null
                                    ? getString(R.string.community_add_post_location_placeholder)
                                    : selectedLocation
                    );
                    Toast.makeText(this, R.string.report_location_selected, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void submitPost() {
        if (isSubmitting || communityRepository == null) {
            return;
        }

        if (titleEditText == null || descriptionEditText == null || locationValueView == null) {
            return;
        }

        String tieuDe = trimToNull(getText(titleEditText));
        String noiDung = trimToNull(getText(descriptionEditText));
        String diaChi = trimToNull(locationValueView.getText() == null ? null : locationValueView.getText().toString());
        if (TextUtils.equals(diaChi, getString(R.string.community_add_post_location_placeholder))) {
            diaChi = null;
        }

        if (tieuDe == null) {
            titleEditText.setError(getString(R.string.community_post_title_required));
            titleEditText.requestFocus();
            return;
        }

        if (noiDung == null) {
            descriptionEditText.setError(getString(R.string.community_post_description_required));
            descriptionEditText.requestFocus();
            return;
        }

        setSubmittingState(true);
        communityRepository.createPost(
                new CommunityRepository.CreateCommunityPostInput(tieuDe, noiDung, diaChi, selectedMediaUri),
                new ResultCallback<>() {
                    @Override
                    public void onSuccess(com.example.cuutro.features.community.model.CommunityPostItem data) {
                        if (isFinishing()) {
                            return;
                        }
                        Toast.makeText(
                                AddNewCommunityActivity.this,
                                R.string.community_post_created_toast,
                                Toast.LENGTH_SHORT
                        ).show();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (isFinishing()) {
                            return;
                        }
                        setSubmittingState(false);
                        if (error.isUnauthorized()) {
                            startActivity(
                                    NotificationScreenActivity.createUnauthorizedIntent(
                                            AddNewCommunityActivity.this,
                                            getString(R.string.auth_session_expired)
                                    )
                            );
                            finish();
                            return;
                        }
                        Toast.makeText(
                                AddNewCommunityActivity.this,
                                getString(R.string.community_create_post_failed, error.getMessage()),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void setSubmittingState(boolean submitting) {
        isSubmitting = submitting;
        if (submitButton != null) {
            submitButton.setEnabled(!submitting);
            submitButton.setText(
                    submitting
                            ? R.string.community_create_post_loading
                            : R.string.community_add_post_submit
            );
        }
        if (backButton != null) {
            backButton.setEnabled(!submitting);
        }
        if (titleEditText != null) {
            titleEditText.setEnabled(!submitting);
        }
        if (descriptionEditText != null) {
            descriptionEditText.setEnabled(!submitting);
        }
        if (uploadMediaLayout != null) {
            uploadMediaLayout.setEnabled(!submitting);
        }
        if (locationValueView != null) {
            locationValueView.setEnabled(!submitting);
        }
    }

    private void handleMediaPicked(Uri uri) {
        if (uri == null) {
            return;
        }

        String mimeType = trimToNull(getContentResolver().getType(uri));
        if (!isSupportedMediaType(mimeType)) {
            Toast.makeText(this, R.string.community_post_media_invalid_type, Toast.LENGTH_LONG).show();
            return;
        }

        selectedMediaUri = uri;
        if (selectedMediaView != null) {
            selectedMediaView.setText(
                    getString(R.string.community_post_media_selected, resolveFileName(uri))
            );
            selectedMediaView.setVisibility(View.VISIBLE);
        }
    }

    private boolean isSupportedMediaType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        String normalized = mimeType.toLowerCase(Locale.ROOT);
        return normalized.startsWith("image/") || normalized.startsWith("video/");
    }

    @NonNull
    private String resolveFileName(@NonNull Uri uri) {
        String fallback = getString(R.string.community_post_media_default_name);
        if (uri.getScheme() == null) {
            return fallback;
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            try (android.database.Cursor cursor = getContentResolver().query(
                    uri,
                    new String[]{OpenableColumns.DISPLAY_NAME},
                    null,
                    null,
                    null
            )) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        String value = trimToNull(cursor.getString(index));
                        if (value != null) {
                            return value;
                        }
                    }
                }
            } catch (Exception ignored) {
                // Ignore and fallback.
            }
        }
        String segment = trimToNull(uri.getLastPathSegment());
        return segment == null ? fallback : segment;
    }

    private String getText(@NonNull EditText editText) {
        Editable editable = editText.getText();
        return editable == null ? "" : editable.toString();
    }

    private String getText(@NonNull TextView textView) {
        CharSequence text = textView.getText();
        return text == null ? "" : text.toString();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
