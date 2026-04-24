package com.example.cuutro.features.report.ui.controller;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cuutro.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportAttachmentSectionController {

    private static final int MAX_IMAGE_ATTACHMENTS = 3;

    private final AppCompatActivity activity;
    private final ExecutorService attachmentPreviewExecutor = Executors.newSingleThreadExecutor();
    private final List<Uri> selectedImageUris = new ArrayList<>();

    private final LinearLayout attachImageButton;
    private final LinearLayout attachVideoButton;
    private final LinearLayout attachAudioButton;
    private final HorizontalScrollView attachmentMediaScrollView;
    private final LinearLayout attachmentMediaContainer;
    private final LinearLayout attachmentAudioContainer;
    private final ImageButton attachmentAudioPlayButton;
    private final TextView attachmentAudioNameText;
    private final TextView attachmentAudioRemoveText;

    private final ActivityResultLauncher<String[]> pickImageAttachmentsLauncher;
    private final ActivityResultLauncher<String[]> pickVideoAttachmentLauncher;
    private final ActivityResultLauncher<String[]> pickAudioAttachmentLauncher;

    @Nullable
    private Uri selectedVideoUri;
    @Nullable
    private Uri selectedAudioUri;
    @Nullable
    private String selectedAudioFileName;
    @Nullable
    private MediaPlayer audioPlayer;
    private boolean audioPlaybackReady;
    private boolean audioPlaying;

    public ReportAttachmentSectionController(@NonNull AppCompatActivity activity) {
        this.activity = activity;
        attachImageButton = activity.findViewById(R.id.layoutAttachImageButton);
        attachVideoButton = activity.findViewById(R.id.layoutAttachVideoButton);
        attachAudioButton = activity.findViewById(R.id.layoutAttachAudioButton);
        attachmentMediaScrollView = activity.findViewById(R.id.scrollReportAttachmentMedia);
        attachmentMediaContainer = activity.findViewById(R.id.layoutReportAttachmentMedia);
        attachmentAudioContainer = activity.findViewById(R.id.layoutReportAttachmentAudio);
        attachmentAudioPlayButton = activity.findViewById(R.id.btnReportAttachmentAudioPlay);
        attachmentAudioNameText = activity.findViewById(R.id.tvReportAttachmentAudioName);
        attachmentAudioRemoveText = activity.findViewById(R.id.tvReportAttachmentAudioRemove);

        pickImageAttachmentsLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                this::onImageAttachmentsPicked
        );
        pickVideoAttachmentLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::onVideoAttachmentPicked
        );
        pickAudioAttachmentLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::onAudioAttachmentPicked
        );

        bindActions();
        renderAttachmentPreview();
    }

    public void onPause() {
        pauseAudioPlayback();
    }

    public void onDestroy() {
        attachmentPreviewExecutor.shutdownNow();
        releaseAudioPlayer();
    }

    @NonNull
    public List<Uri> getSelectedImageUris() {
        return new ArrayList<>(selectedImageUris);
    }

    @Nullable
    public Uri getSelectedVideoUri() {
        return selectedVideoUri;
    }

    @Nullable
    public Uri getSelectedAudioUri() {
        return selectedAudioUri;
    }

    public boolean hasAnyAttachment() {
        return !selectedImageUris.isEmpty() || selectedVideoUri != null || selectedAudioUri != null;
    }

    private void bindActions() {
        if (attachImageButton != null) {
            attachImageButton.setOnClickListener(v -> openImageAttachmentPicker());
        }
        if (attachVideoButton != null) {
            attachVideoButton.setOnClickListener(v -> openVideoAttachmentPicker());
        }
        if (attachAudioButton != null) {
            attachAudioButton.setOnClickListener(v -> openAudioAttachmentPicker());
        }
        if (attachmentAudioPlayButton != null) {
            attachmentAudioPlayButton.setOnClickListener(v -> toggleAudioPlayback());
        }
        if (attachmentAudioRemoveText != null) {
            attachmentAudioRemoveText.setOnClickListener(v -> removeSelectedAudioAttachment());
        }
    }

    private void onImageAttachmentsPicked(@Nullable List<Uri> uris) {
        if (uris == null || uris.isEmpty()) {
            return;
        }
        int remaining = MAX_IMAGE_ATTACHMENTS - selectedImageUris.size();
        if (remaining <= 0) {
            Toast.makeText(activity, R.string.report_attachment_image_limit_reached, Toast.LENGTH_SHORT).show();
            return;
        }

        int added = 0;
        boolean overflow = false;
        for (Uri uri : uris) {
            if (uri == null || containsUri(selectedImageUris, uri)) {
                continue;
            }
            if (added >= remaining) {
                overflow = true;
                continue;
            }
            persistReadPermission(uri);
            selectedImageUris.add(uri);
            added++;
        }

        if (overflow) {
            Toast.makeText(activity, R.string.report_attachment_image_limit_overflow, Toast.LENGTH_SHORT).show();
        }
        renderAttachmentPreview();
    }

    private void onVideoAttachmentPicked(@Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        if (selectedVideoUri != null) {
            Toast.makeText(activity, R.string.report_attachment_video_limit_reached, Toast.LENGTH_SHORT).show();
            return;
        }
        persistReadPermission(uri);
        selectedVideoUri = uri;
        renderAttachmentPreview();
    }

    private void onAudioAttachmentPicked(@Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        if (selectedAudioUri != null) {
            Toast.makeText(activity, R.string.report_attachment_audio_limit_reached, Toast.LENGTH_SHORT).show();
            return;
        }
        persistReadPermission(uri);
        selectedAudioUri = uri;
        selectedAudioFileName = resolveAttachmentName(
                uri,
                activity.getString(R.string.report_attachment_audio_default_name)
        );
        releaseAudioPlayer();
        renderAttachmentPreview();
    }

    private void openImageAttachmentPicker() {
        if (selectedImageUris.size() >= MAX_IMAGE_ATTACHMENTS) {
            Toast.makeText(activity, R.string.report_attachment_image_limit_reached, Toast.LENGTH_SHORT).show();
            return;
        }
        pickImageAttachmentsLauncher.launch(new String[] {"image/*"});
    }

    private void openVideoAttachmentPicker() {
        if (selectedVideoUri != null) {
            Toast.makeText(activity, R.string.report_attachment_video_limit_reached, Toast.LENGTH_SHORT).show();
            return;
        }
        pickVideoAttachmentLauncher.launch(new String[] {"video/*"});
    }

    private void openAudioAttachmentPicker() {
        if (selectedAudioUri != null) {
            Toast.makeText(activity, R.string.report_attachment_audio_limit_reached, Toast.LENGTH_SHORT).show();
            return;
        }
        pickAudioAttachmentLauncher.launch(new String[] {"audio/*"});
    }

    private void renderAttachmentPreview() {
        renderMediaAttachmentPreview();
        renderAudioAttachmentPreview();
    }

    private void renderMediaAttachmentPreview() {
        if (attachmentMediaContainer == null || attachmentMediaScrollView == null) {
            return;
        }

        attachmentMediaContainer.removeAllViews();
        for (Uri imageUri : selectedImageUris) {
            addMediaAttachmentView(imageUri, false);
        }
        if (selectedVideoUri != null) {
            addMediaAttachmentView(selectedVideoUri, true);
        }

        attachmentMediaScrollView.setVisibility(
                attachmentMediaContainer.getChildCount() == 0 ? View.GONE : View.VISIBLE
        );
    }

    private void addMediaAttachmentView(@NonNull Uri uri, boolean isVideo) {
        if (attachmentMediaContainer == null) {
            return;
        }
        View itemView = LayoutInflater.from(activity).inflate(
                R.layout.item_report_attachment_media,
                attachmentMediaContainer,
                false
        );
        ImageView previewImage = itemView.findViewById(R.id.ivReportAttachmentPreview);
        ImageView videoOverlay = itemView.findViewById(R.id.ivReportAttachmentVideoOverlay);
        ImageButton removeButton = itemView.findViewById(R.id.btnReportAttachmentRemove);

        if (isVideo) {
            videoOverlay.setVisibility(View.VISIBLE);
            bindVideoAttachmentPreview(previewImage, uri);
            removeButton.setOnClickListener(v -> removeSelectedVideoAttachment());
        } else {
            videoOverlay.setVisibility(View.GONE);
            bindImageAttachmentPreview(previewImage, uri);
            removeButton.setOnClickListener(v -> removeSelectedImageAttachment(uri));
        }

        attachmentMediaContainer.addView(itemView);
    }

    private void bindImageAttachmentPreview(@NonNull ImageView previewImage, @NonNull Uri imageUri) {
        previewImage.setTag("image:" + imageUri);
        previewImage.setImageTintList(null);
        previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        previewImage.setImageURI(imageUri);
    }

    private void bindVideoAttachmentPreview(@NonNull ImageView previewImage, @NonNull Uri videoUri) {
        String expectedTag = "video:" + videoUri;
        previewImage.setTag(expectedTag);
        previewImage.setImageResource(R.drawable.ic_report_video);
        previewImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        previewImage.setImageTintList(
                ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.report_action_red))
        );

        attachmentPreviewExecutor.execute(() -> {
            Bitmap thumbnail = extractVideoThumbnail(videoUri);
            activity.runOnUiThread(() -> {
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
                Object tag = previewImage.getTag();
                if (!(tag instanceof String) || !expectedTag.equals(tag)) {
                    return;
                }
                if (thumbnail == null) {
                    return;
                }
                previewImage.setImageBitmap(thumbnail);
                previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                previewImage.setImageTintList(null);
            });
        });
    }

    @Nullable
    private Bitmap extractVideoThumbnail(@NonNull Uri videoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(activity, videoUri);
            Bitmap bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (bitmap != null) {
                return bitmap;
            }
            return retriever.getFrameAtTime();
        } catch (RuntimeException ignored) {
            return null;
        } finally {
            try {
                retriever.release();
            } catch (IOException | RuntimeException ignored) {
                // Ignore.
            }
        }
    }

    private void removeSelectedImageAttachment(@NonNull Uri targetUri) {
        for (int i = 0; i < selectedImageUris.size(); i++) {
            if (sameUri(selectedImageUris.get(i), targetUri)) {
                selectedImageUris.remove(i);
                break;
            }
        }
        renderMediaAttachmentPreview();
    }

    private void removeSelectedVideoAttachment() {
        selectedVideoUri = null;
        renderMediaAttachmentPreview();
    }

    private void renderAudioAttachmentPreview() {
        if (attachmentAudioContainer == null) {
            return;
        }
        if (selectedAudioUri == null) {
            attachmentAudioContainer.setVisibility(View.GONE);
            updateAudioPlaybackButton();
            return;
        }

        attachmentAudioContainer.setVisibility(View.VISIBLE);
        if (attachmentAudioNameText != null) {
            String fallbackName = activity.getString(R.string.report_attachment_audio_default_name);
            String audioName = selectedAudioFileName;
            if (isBlank(audioName)) {
                audioName = resolveAttachmentName(selectedAudioUri, fallbackName);
                selectedAudioFileName = audioName;
            }
            attachmentAudioNameText.setText(audioName);
        }
        updateAudioPlaybackButton();
    }

    private void toggleAudioPlayback() {
        if (selectedAudioUri == null) {
            return;
        }
        if (audioPlayer != null && audioPlaybackReady) {
            if (audioPlaying) {
                pauseAudioPlayback();
            } else {
                try {
                    audioPlayer.start();
                    audioPlaying = true;
                    updateAudioPlaybackButton();
                } catch (IllegalStateException ignored) {
                    releaseAudioPlayer();
                }
            }
            return;
        }
        startAudioPlayback(selectedAudioUri);
    }

    private void startAudioPlayback(@NonNull Uri audioUri) {
        releaseAudioPlayer();
        MediaPlayer player = new MediaPlayer();
        audioPlayer = player;
        audioPlaybackReady = false;
        audioPlaying = false;
        updateAudioPlaybackButton();

        player.setOnPreparedListener(mp -> {
            audioPlaybackReady = true;
            try {
                mp.start();
                audioPlaying = true;
            } catch (IllegalStateException ignored) {
                releaseAudioPlayer();
                return;
            }
            updateAudioPlaybackButton();
        });
        player.setOnCompletionListener(mp -> {
            audioPlaying = false;
            if (audioPlaybackReady) {
                try {
                    mp.seekTo(0);
                } catch (IllegalStateException ignored) {
                    // Ignore.
                }
            }
            updateAudioPlaybackButton();
        });
        player.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(activity, R.string.report_attachment_audio_play_failed, Toast.LENGTH_SHORT).show();
            releaseAudioPlayer();
            return true;
        });

        try {
            player.setDataSource(activity, audioUri);
            player.prepareAsync();
        } catch (IOException | SecurityException | IllegalStateException e) {
            Toast.makeText(activity, R.string.report_attachment_audio_play_failed, Toast.LENGTH_SHORT).show();
            releaseAudioPlayer();
        }
    }

    private void pauseAudioPlayback() {
        if (audioPlayer == null || !audioPlaybackReady) {
            return;
        }
        try {
            audioPlayer.pause();
            audioPlaying = false;
            updateAudioPlaybackButton();
        } catch (IllegalStateException ignored) {
            releaseAudioPlayer();
        }
    }

    private void removeSelectedAudioAttachment() {
        selectedAudioUri = null;
        selectedAudioFileName = null;
        releaseAudioPlayer();
        renderAudioAttachmentPreview();
    }

    private void updateAudioPlaybackButton() {
        if (attachmentAudioPlayButton == null) {
            return;
        }
        if (audioPlaying) {
            attachmentAudioPlayButton.setImageResource(R.drawable.ic_report_audio_pause);
            attachmentAudioPlayButton.setContentDescription(activity.getString(R.string.report_attachment_audio_pause));
            return;
        }
        attachmentAudioPlayButton.setImageResource(R.drawable.ic_report_audio_play);
        attachmentAudioPlayButton.setContentDescription(activity.getString(R.string.report_attachment_audio_play));
    }

    private void releaseAudioPlayer() {
        if (audioPlayer != null) {
            try {
                if (audioPlaybackReady) {
                    audioPlayer.stop();
                }
            } catch (IllegalStateException ignored) {
                // Ignore.
            }
            try {
                audioPlayer.reset();
            } catch (IllegalStateException ignored) {
                // Ignore.
            }
            audioPlayer.release();
            audioPlayer = null;
        }
        audioPlaybackReady = false;
        audioPlaying = false;
        updateAudioPlaybackButton();
    }

    private void persistReadPermission(@NonNull Uri uri) {
        try {
            activity.getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (SecurityException | IllegalArgumentException ignored) {
            // Some document providers do not support persistable permissions.
        }
    }

    private boolean containsUri(@NonNull List<Uri> uris, @NonNull Uri candidate) {
        for (Uri uri : uris) {
            if (sameUri(uri, candidate)) {
                return true;
            }
        }
        return false;
    }

    private boolean sameUri(@Nullable Uri first, @Nullable Uri second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return first.toString().equals(second.toString());
    }

    @NonNull
    private String resolveAttachmentName(@NonNull Uri uri, @NonNull String fallback) {
        Cursor cursor = null;
        try {
            cursor = activity.getContentResolver().query(
                    uri,
                    new String[] {OpenableColumns.DISPLAY_NAME},
                    null,
                    null,
                    null
            );
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String name = cursor.getString(index);
                    if (!isBlank(name)) {
                        return name.trim();
                    }
                }
            }
        } catch (SecurityException ignored) {
            // Ignore and fallback to URI path.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        String lastPath = uri.getLastPathSegment();
        if (!isBlank(lastPath)) {
            return lastPath.trim();
        }
        return fallback;
    }

    private boolean isBlank(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }
}
