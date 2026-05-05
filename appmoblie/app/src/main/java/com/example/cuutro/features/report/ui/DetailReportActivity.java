package com.example.cuutro.features.report.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.chat.ui.ChatsActivity;
import com.example.cuutro.features.report.ui.controller.ReportBitmapLoader;
import com.example.cuutro.features.sos.data.SosRepository;
import com.example.cuutro.features.sos.model.EmergencyReportDetail;
import com.example.cuutro.features.sos.model.EmergencyReportItem;
import com.example.cuutro.features.sos.model.EmergencyReportSupplyItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DetailReportActivity extends AppCompatActivity {

    private static final String EXTRA_REPORT_ID = "extra_report_id";
    private static final String EXTRA_REPORT_TITLE = "extra_report_title";
    private static final String EXTRA_REPORT_LOCATION = "extra_report_location";
    private static final String EXTRA_REPORT_DESCRIPTION = "extra_report_description";
    private static final String EXTRA_REPORT_STATUS = "extra_report_status";
    private static final int MAX_SCENE_IMAGES = 4;

    private final DateTimeFormatter createdAtFormatter = DateTimeFormatter
            .ofPattern("dd/MM/yyyy - HH:mm", Locale.getDefault())
            .withZone(ZoneId.systemDefault());

    private SosRepository sosRepository;
    private ReportBitmapLoader bitmapLoader;

    private MaterialCardView statusChipCard;
    private TextView reportCodeText;
    private TextView reportStatusText;
    private TextView createdAtText;
    private TextView reporterNameText;
    private TextView reporterPhoneText;
    private TextView locationText;
    private TextView incidentTypeText;
    private ImageView incidentTypeBadgeView;
    private TextView rescueTeamNameText;
    private TextView reportNoteText;
    private TextView sceneImageCountText;
    private TextView currentStatusText;
    private TextView supplyEmptyStateText;
    private ImageView[] sceneImageViews;
    private MaterialButton hotlineButton;
    private MaterialButton contactRescueTeamButton;
    private MaterialButton supplyDetailToggleButton;
    private LinearLayout supplyItemsContainer;

    private String reportId;
    private String hotlinePhone;
    private String rescueTeamPhone;
    private boolean isSupplyExpanded = false;
    @NonNull
    private List<EmergencyReportSupplyItem> currentSupplyItems = Collections.emptyList();

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull EmergencyReportItem item) {
        Intent intent = new Intent(context, DetailReportActivity.class);
        intent.putExtra(EXTRA_REPORT_ID, item.getId());
        intent.putExtra(EXTRA_REPORT_TITLE, item.getTitle());
        intent.putExtra(EXTRA_REPORT_LOCATION, item.getLocation());
        intent.putExtra(EXTRA_REPORT_DESCRIPTION, item.getDescription());
        intent.putExtra(EXTRA_REPORT_STATUS, item.getStatus());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MyApp app = (MyApp) getApplication();
        sosRepository = app.getAppContainer().getSosRepository();
        bitmapLoader = new ReportBitmapLoader();

        hotlinePhone = getString(R.string.detail_report_hotline_phone);
        rescueTeamPhone = getString(R.string.detail_report_rescue_team_phone);

        bindViews();
        setupActions();
        bindFallbackFromIntent();
        loadReportDetail();
    }

    @Override
    protected void onDestroy() {
        if (bitmapLoader != null) {
            bitmapLoader.shutdown();
        }
        super.onDestroy();
    }

    private void bindViews() {
        statusChipCard = findViewById(R.id.cardReportStatusChip);
        reportCodeText = findViewById(R.id.tvReportCode);
        reportStatusText = findViewById(R.id.tvReportStatus);
        createdAtText = findViewById(R.id.tvCreatedAt);
        reporterNameText = findViewById(R.id.tvReporterName);
        reporterPhoneText = findViewById(R.id.tvReporterPhone);
        locationText = findViewById(R.id.tvLocationValue);
        incidentTypeText = findViewById(R.id.tvIncidentType);
        incidentTypeBadgeView = findViewById(R.id.ivIncidentTypeBadge);
        rescueTeamNameText = findViewById(R.id.tvRescueTeamName);
        reportNoteText = findViewById(R.id.tvReportNote);
        sceneImageCountText = findViewById(R.id.tvSceneImageCount);
        currentStatusText = findViewById(R.id.tvCurrentStatus);
        supplyEmptyStateText = findViewById(R.id.tvSupplyEmptyState);
        hotlineButton = findViewById(R.id.btnHotlineCall);
        contactRescueTeamButton = findViewById(R.id.btnContactRescueTeam);
        supplyDetailToggleButton = findViewById(R.id.btnSupplyDetailToggle);
        supplyItemsContainer = findViewById(R.id.layoutSupplyItems);
        sceneImageViews = new ImageView[]{
                findViewById(R.id.ivSceneImage1),
                findViewById(R.id.ivSceneImage2),
                findViewById(R.id.ivSceneImage3),
                findViewById(R.id.ivSceneImage4)
        };
    }

    private void setupActions() {
        ImageButton backButton = findViewById(R.id.btnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (hotlineButton != null) {
            hotlineButton.setOnClickListener(v -> openDialer(hotlinePhone));
        }
        if (contactRescueTeamButton != null) {
            contactRescueTeamButton.setOnClickListener(v -> openChatForCurrentReport());
        }
        if (supplyDetailToggleButton != null) {
            supplyDetailToggleButton.setOnClickListener(v -> {
                if (currentSupplyItems.isEmpty()) {
                    return;
                }
                isSupplyExpanded = !isSupplyExpanded;
                renderSupplyItems();
            });
        }
    }

    private void bindFallbackFromIntent() {
        Intent intent = getIntent();
        reportId = intent == null ? null : intent.getStringExtra(EXTRA_REPORT_ID);

        String fallbackTitle = intent == null ? null : intent.getStringExtra(EXTRA_REPORT_TITLE);
        String fallbackLocation = intent == null ? null : intent.getStringExtra(EXTRA_REPORT_LOCATION);
        String fallbackDescription = intent == null ? null : intent.getStringExtra(EXTRA_REPORT_DESCRIPTION);
        String fallbackStatus = intent == null ? null : intent.getStringExtra(EXTRA_REPORT_STATUS);

        String safeReportId = fallbackIfBlank(reportId, "--");
        String safeTitle = fallbackIfBlank(fallbackTitle, getString(R.string.detail_report_unknown_value));
        String safeLocation = fallbackIfBlank(fallbackLocation, getString(R.string.detail_report_unknown_value));
        String safeDescription = fallbackIfBlank(fallbackDescription, getString(R.string.detail_report_unknown_value));
        String statusLabel = resolveStatusLabel(fallbackStatus);

        if (reportCodeText != null) {
            reportCodeText.setText(getString(R.string.detail_report_ticket_code, safeReportId));
        }
        if (incidentTypeText != null) {
            incidentTypeText.setText(safeTitle);
        }
        bindIncidentTypeBadge(null);
        if (locationText != null) {
            locationText.setText(safeLocation);
        }
        if (reportNoteText != null) {
            reportNoteText.setText(safeDescription);
        }
        if (createdAtText != null) {
            createdAtText.setText(getString(R.string.detail_report_unknown_created_at));
        }
        if (reporterNameText != null) {
            reporterNameText.setText(getString(R.string.detail_report_unknown_value));
        }
        if (reporterPhoneText != null) {
            reporterPhoneText.setText(getString(R.string.detail_report_unknown_value));
        }
        if (rescueTeamNameText != null) {
            rescueTeamNameText.setText(getString(R.string.detail_report_default_team));
        }

        applyStatusVisual(fallbackStatus, statusLabel);
        bindSupplyItems(Collections.emptyList());
        bindSceneImages(Collections.emptyList());
    }

    private void loadReportDetail() {
        if (sosRepository == null || TextUtils.isEmpty(reportId) || !reportId.matches("\\d+")) {
            return;
        }

        sosRepository.getEmergencyReportDetail(reportId, new ResultCallback<EmergencyReportDetail>() {
            @Override
            public void onSuccess(EmergencyReportDetail data) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                bindReportDetail(data);
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Toast.makeText(
                        DetailReportActivity.this,
                        getString(R.string.detail_report_load_failed, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void bindReportDetail(@NonNull EmergencyReportDetail detail) {
        reportId = detail.getId();
        if (reportCodeText != null) {
            reportCodeText.setText(getString(R.string.detail_report_ticket_code, detail.getId()));
        }
        if (incidentTypeText != null) {
            incidentTypeText.setText(fallbackIfBlank(
                    detail.getTitle(),
                    getString(R.string.detail_report_unknown_value)
            ));
        }
        bindIncidentTypeBadge(detail.getIncidentIconUrl());
        if (locationText != null) {
            locationText.setText(fallbackIfBlank(
                    detail.getLocation(),
                    getString(R.string.detail_report_unknown_value)
            ));
        }
        if (reportNoteText != null) {
            reportNoteText.setText(fallbackIfBlank(
                    detail.getDescription(),
                    getString(R.string.detail_report_unknown_value)
            ));
        }
        if (createdAtText != null) {
            createdAtText.setText(formatCreatedAt(detail.getCreatedAt()));
        }
        if (reporterNameText != null) {
            reporterNameText.setText(fallbackIfBlank(
                    detail.getReporterName(),
                    getString(R.string.detail_report_unknown_value)
            ));
        }

        String reporterPhone = trimToNull(detail.getReporterPhone());
        if (reporterPhoneText != null) {
            reporterPhoneText.setText(reporterPhone == null
                    ? getString(R.string.detail_report_unknown_value)
                    : reporterPhone);
        }

        applyStatusVisual(detail.getStatus(), detail.getStatusLabel());
        bindSupplyItems(detail.getSupplyItems());
        bindSceneImages(detail.getImageUrls());
    }

    private void bindSupplyItems(@Nullable List<EmergencyReportSupplyItem> supplyItems) {
        if (supplyItems == null || supplyItems.isEmpty()) {
            currentSupplyItems = Collections.emptyList();
            isSupplyExpanded = false;
            renderSupplyItems();
            return;
        }
        currentSupplyItems = supplyItems;
        isSupplyExpanded = true;
        renderSupplyItems();
    }

    private void renderSupplyItems() {
        if (supplyItemsContainer == null || supplyDetailToggleButton == null || supplyEmptyStateText == null) {
            return;
        }

        boolean hasData = !currentSupplyItems.isEmpty();
        supplyDetailToggleButton.setVisibility(hasData ? View.VISIBLE : View.GONE);
        supplyItemsContainer.setVisibility(hasData && isSupplyExpanded ? View.VISIBLE : View.GONE);
        supplyEmptyStateText.setVisibility(hasData ? View.GONE : View.VISIBLE);

        supplyDetailToggleButton.setText(
                isSupplyExpanded
                        ? R.string.detail_report_supply_toggle_collapse
                        : R.string.detail_report_supply_toggle_expand
        );
        supplyDetailToggleButton.setIconResource(
                isSupplyExpanded
                        ? android.R.drawable.arrow_up_float
                        : android.R.drawable.arrow_down_float
        );

        supplyItemsContainer.removeAllViews();
        if (!hasData || !isSupplyExpanded) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < currentSupplyItems.size(); i++) {
            EmergencyReportSupplyItem item = currentSupplyItems.get(i);
            View itemView = inflater.inflate(R.layout.item_detail_report_supply, supplyItemsContainer, false);

            TextView nameText = itemView.findViewById(R.id.tvSupplyItemName);
            TextView metaText = itemView.findViewById(R.id.tvSupplyItemMeta);
            TextView qtyText = itemView.findViewById(R.id.tvSupplyItemQty);
            ImageView iconView = itemView.findViewById(R.id.ivSupplyItemIcon);

            String supplyName = fallbackIfBlank(
                    item.getName(),
                    getString(R.string.detail_report_supply_item_unknown_name)
            );
            String supplyNote = fallbackIfBlank(
                    item.getNote(),
                    getString(R.string.detail_report_supply_item_note_fallback)
            );

            nameText.setText(supplyName);
            metaText.setText(supplyNote);
            bindSupplyItemIcon(iconView, item.getIconUrl());

            Integer quantity = item.getQuantity();
            qtyText.setText(quantity == null
                    ? "-"
                    : getString(R.string.detail_report_supply_item_qty_short, quantity));

            supplyItemsContainer.addView(itemView);
            if (i < currentSupplyItems.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(1)
                );
                params.setMargins(dpToPx(8), 0, dpToPx(8), 0);
                divider.setLayoutParams(params);
                divider.setBackgroundColor(0xFFE5E7EB);
                supplyItemsContainer.addView(divider);
            }
        }
    }

    private void bindIncidentTypeBadge(@Nullable String rawUrl) {
        if (incidentTypeBadgeView == null) {
            return;
        }
        bindRemoteIcon(
                incidentTypeBadgeView,
                rawUrl,
                android.R.drawable.ic_menu_agenda,
                R.color.color_primary,
                8,
                6,
                ImageView.ScaleType.CENTER_INSIDE
        );
    }

    private void bindSupplyItemIcon(@Nullable ImageView imageView, @Nullable String rawUrl) {
        if (imageView == null) {
            return;
        }
        bindRemoteIcon(
                imageView,
                rawUrl,
                R.drawable.ic_emergency_other,
                R.color.report_hint,
                9,
                2,
                ImageView.ScaleType.CENTER_CROP
        );
    }

    private void bindRemoteIcon(
            @NonNull ImageView imageView,
            @Nullable String rawUrl,
            int placeholderResId,
            int placeholderTintColorRes,
            int placeholderPaddingDp,
            int loadedPaddingDp,
            @NonNull ImageView.ScaleType loadedScaleType
    ) {
        if (bitmapLoader == null) {
            setPlaceholderIcon(imageView, placeholderResId, placeholderTintColorRes, placeholderPaddingDp);
            return;
        }

        String targetUrl = bitmapLoader.normalizeUrl(rawUrl);
        if (targetUrl == null || !bitmapLoader.isHttpUrl(targetUrl)) {
            setPlaceholderIcon(imageView, placeholderResId, placeholderTintColorRes, placeholderPaddingDp);
            return;
        }
        imageView.setTag(targetUrl);

        bitmapLoader.load(rawUrl, (loadedUrl, bitmap) -> {
            Object currentTag = imageView.getTag();
            if (!(currentTag instanceof String)) {
                return;
            }
            String expectedUrl = (String) currentTag;
            if (loadedUrl == null || !expectedUrl.equals(loadedUrl)) {
                return;
            }
            if (bitmap == null) {
                setPlaceholderIcon(imageView, placeholderResId, placeholderTintColorRes, placeholderPaddingDp);
                return;
            }
            imageView.setImageBitmap(bitmap);
            imageView.setImageTintList(null);
            imageView.setScaleType(loadedScaleType);
            int padding = dpToPx(loadedPaddingDp);
            imageView.setPadding(padding, padding, padding, padding);
        });
    }

    private void setPlaceholderIcon(
            @NonNull ImageView imageView,
            int placeholderResId,
            int placeholderTintColorRes,
            int paddingDp
    ) {
        imageView.setTag(null);
        imageView.setImageResource(placeholderResId);
        imageView.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, placeholderTintColorRes)
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int padding = dpToPx(paddingDp);
        imageView.setPadding(padding, padding, padding, padding);
    }

    private void applyStatusVisual(@Nullable String rawStatus, @Nullable String statusLabel) {
        String resolvedStatusLabel = fallbackIfBlank(statusLabel, resolveStatusLabel(rawStatus));
        if (reportStatusText != null) {
            reportStatusText.setText(resolvedStatusLabel);
        }
        if (currentStatusText != null) {
            currentStatusText.setText(
                    getString(R.string.detail_report_status_prefix, resolvedStatusLabel)
            );
        }

        int chipBackgroundColor;
        int chipTextColor;
        String normalized = rawStatus == null ? "" : rawStatus.trim().toUpperCase(Locale.ROOT);
        if ("HOAN_THANH".equals(normalized)) {
            chipBackgroundColor = 0xFFE8F6EE;
            chipTextColor = 0xFF15803D;
        } else if ("HUY".equals(normalized)) {
            chipBackgroundColor = 0xFFF3F4F6;
            chipTextColor = 0xFF6B7280;
        } else {
            chipBackgroundColor = 0xFFFDECEC;
            chipTextColor = ContextCompat.getColor(this, R.color.color_primary);
        }

        if (statusChipCard != null) {
            statusChipCard.setCardBackgroundColor(chipBackgroundColor);
        }
        if (reportStatusText != null) {
            reportStatusText.setTextColor(chipTextColor);
        }
    }

    private void bindSceneImages(@Nullable List<String> imageUrls) {
        int totalCount = imageUrls == null ? 0 : imageUrls.size();
        if (sceneImageCountText != null) {
            sceneImageCountText.setText(getString(R.string.detail_report_image_count, totalCount));
        }

        for (ImageView imageView : sceneImageViews) {
            setPlaceholderImage(imageView);
        }

        if (imageUrls == null || imageUrls.isEmpty() || bitmapLoader == null) {
            return;
        }

        int renderCount = Math.min(MAX_SCENE_IMAGES, imageUrls.size());
        for (int i = 0; i < renderCount; i++) {
            loadSceneImage(sceneImageViews[i], imageUrls.get(i));
        }
    }

    private void loadSceneImage(@NonNull ImageView imageView, @Nullable String rawUrl) {
        if (bitmapLoader == null) {
            setPlaceholderImage(imageView);
            return;
        }

        String targetUrl = bitmapLoader.normalizeUrl(rawUrl);
        if (targetUrl == null) {
            setPlaceholderImage(imageView);
            return;
        }
        imageView.setTag(targetUrl);

        bitmapLoader.load(rawUrl, (loadedUrl, bitmap) -> {
            Object currentTag = imageView.getTag();
            if (!(currentTag instanceof String)) {
                return;
            }
            String expectedUrl = (String) currentTag;
            if (loadedUrl == null || !expectedUrl.equals(loadedUrl)) {
                return;
            }
            if (bitmap == null) {
                setPlaceholderImage(imageView);
                return;
            }
            bindBitmap(imageView, bitmap);
        });
    }

    private void bindBitmap(@NonNull ImageView imageView, @NonNull Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        imageView.setImageTintList(null);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int padding = dpToPx(2);
        imageView.setPadding(padding, padding, padding, padding);
    }

    private void setPlaceholderImage(@NonNull ImageView imageView) {
        imageView.setTag(null);
        imageView.setImageResource(R.drawable.ic_report_camera);
        imageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.report_hint)));
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int padding = dpToPx(18);
        imageView.setPadding(padding, padding, padding, padding);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void openDialer(@Nullable String rawPhone) {
        String normalizedPhone = normalizePhone(rawPhone);
        if (TextUtils.isEmpty(normalizedPhone)) {
            Toast.makeText(this, R.string.detail_report_phone_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + normalizedPhone));
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, R.string.detail_report_dialer_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
    }

    private void openChatForCurrentReport() {
        String normalizedReportId = trimToNull(reportId);
        if (normalizedReportId == null) {
            Toast.makeText(this, R.string.chat_invalid_report_id, Toast.LENGTH_SHORT).show();
            return;
        }
        long parsedReportId;
        try {
            parsedReportId = Long.parseLong(normalizedReportId);
        } catch (NumberFormatException ignored) {
            Toast.makeText(this, R.string.chat_invalid_report_id, Toast.LENGTH_SHORT).show();
            return;
        }
        if (parsedReportId <= 0L) {
            Toast.makeText(this, R.string.chat_invalid_report_id, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(ChatsActivity.createIntent(this, parsedReportId));
    }

    @NonNull
    private String normalizePhone(@Nullable String rawPhone) {
        if (rawPhone == null) {
            return "";
        }
        return rawPhone.replaceAll("[^0-9+]", "");
    }

    @NonNull
    private String resolveStatusLabel(@Nullable String rawStatus) {
        if (sosRepository != null) {
            return sosRepository.resolveStatusLabel(rawStatus);
        }
        return getString(R.string.detail_report_status_processing);
    }

    @NonNull
    private String formatCreatedAt(@Nullable String rawCreatedAt) {
        String normalized = trimToNull(rawCreatedAt);
        if (normalized == null) {
            return getString(R.string.detail_report_unknown_created_at);
        }
        try {
            Instant instant = Instant.parse(normalized);
            return createdAtFormatter.format(instant);
        } catch (Exception ignored) {
            return normalized;
        }
    }

    @Nullable
    private String trimToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @NonNull
    private String fallbackIfBlank(@Nullable String value, @NonNull String fallback) {
        String normalized = trimToNull(value);
        return normalized == null ? fallback : normalized;
    }
}
