package com.example.cuutro.features.chat.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.Objects;

public class ChatConversationItem {

    public enum MediaKind {
        NONE,
        IMAGE,
        VIDEO,
        AUDIO
    }

    @Nullable
    private final Long id;

    @Nullable
    private final Long senderAccountId;

    @NonNull
    private final String senderName;

    @Nullable
    private final String content;

    @Nullable
    private final String mediaUrl;

    @Nullable
    private final String mediaType;

    @NonNull
    private final MediaKind mediaKind;

    private final long createdAtMillis;

    public ChatConversationItem(
            @Nullable Long id,
            @Nullable Long senderAccountId,
            @NonNull String senderName,
            @Nullable String content,
            @Nullable String mediaUrl,
            @Nullable String mediaType,
            @NonNull MediaKind mediaKind,
            long createdAtMillis
    ) {
        this.id = id;
        this.senderAccountId = senderAccountId;
        this.senderName = senderName;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.mediaKind = mediaKind;
        this.createdAtMillis = Math.max(0L, createdAtMillis);
    }

    @Nullable
    public Long getId() {
        return id;
    }

    @Nullable
    public Long getSenderAccountId() {
        return senderAccountId;
    }

    @NonNull
    public String getSenderName() {
        return senderName;
    }

    @Nullable
    public String getContent() {
        return content;
    }

    @Nullable
    public String getMediaUrl() {
        return mediaUrl;
    }

    @Nullable
    public String getMediaType() {
        return mediaType;
    }

    @NonNull
    public MediaKind getMediaKind() {
        return mediaKind;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public boolean isOutgoing(@Nullable Long currentAccountId) {
        if (currentAccountId == null || senderAccountId == null) {
            return false;
        }
        return Objects.equals(currentAccountId, senderAccountId);
    }

    @NonNull
    public static MediaKind resolveMediaKind(@Nullable String mediaType, @Nullable String mediaUrl) {
        String lowerType = mediaType == null ? "" : mediaType.trim().toLowerCase(Locale.ROOT);
        if (!lowerType.isEmpty()) {
            if (lowerType.contains("video")) {
                return MediaKind.VIDEO;
            }
            if (lowerType.contains("audio")) {
                return MediaKind.AUDIO;
            }
            if (lowerType.contains("image") || lowerType.contains("img") || lowerType.contains("photo")) {
                return MediaKind.IMAGE;
            }
        }

        String lowerUrl = mediaUrl == null ? "" : mediaUrl.trim().toLowerCase(Locale.ROOT);
        if (lowerUrl.endsWith(".mp4") || lowerUrl.endsWith(".mov") || lowerUrl.endsWith(".mkv")) {
            return MediaKind.VIDEO;
        }
        if (lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".m4a") || lowerUrl.endsWith(".wav") || lowerUrl.endsWith(".aac")) {
            return MediaKind.AUDIO;
        }
        if (lowerUrl.endsWith(".jpg")
                || lowerUrl.endsWith(".jpeg")
                || lowerUrl.endsWith(".png")
                || lowerUrl.endsWith(".webp")
                || lowerUrl.endsWith(".gif")) {
            return MediaKind.IMAGE;
        }

        return lowerUrl.isEmpty() ? MediaKind.NONE : MediaKind.IMAGE;
    }
}
