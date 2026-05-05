package com.example.cuutro.features.chat.ui;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ChatMessage {

    public enum Type {
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO
    }

    private final long id;
    @NonNull
    private final Type type;
    private final boolean outgoing;
    @NonNull
    private final String senderName;
    @Nullable
    private final String text;
    @Nullable
    private final Uri attachmentUri;
    @Nullable
    private final String attachmentName;
    private final long timestampMs;

    private ChatMessage(
            long id,
            @NonNull Type type,
            boolean outgoing,
            @NonNull String senderName,
            @Nullable String text,
            @Nullable Uri attachmentUri,
            @Nullable String attachmentName,
            long timestampMs
    ) {
        this.id = id;
        this.type = type;
        this.outgoing = outgoing;
        this.senderName = senderName;
        this.text = text;
        this.attachmentUri = attachmentUri;
        this.attachmentName = attachmentName;
        this.timestampMs = timestampMs;
    }

    @NonNull
    public static ChatMessage text(
            long id,
            boolean outgoing,
            @NonNull String senderName,
            @NonNull String text,
            long timestampMs
    ) {
        return new ChatMessage(id, Type.TEXT, outgoing, senderName, text, null, null, timestampMs);
    }

    @NonNull
    public static ChatMessage image(
            long id,
            boolean outgoing,
            @NonNull String senderName,
            @Nullable String caption,
            @NonNull Uri attachmentUri,
            long timestampMs
    ) {
        return new ChatMessage(id, Type.IMAGE, outgoing, senderName, caption, attachmentUri, null, timestampMs);
    }

    @NonNull
    public static ChatMessage video(
            long id,
            boolean outgoing,
            @NonNull String senderName,
            @Nullable String caption,
            @NonNull Uri attachmentUri,
            long timestampMs
    ) {
        return new ChatMessage(id, Type.VIDEO, outgoing, senderName, caption, attachmentUri, null, timestampMs);
    }

    @NonNull
    public static ChatMessage audio(
            long id,
            boolean outgoing,
            @NonNull String senderName,
            @Nullable String caption,
            @NonNull Uri attachmentUri,
            @Nullable String attachmentName,
            long timestampMs
    ) {
        return new ChatMessage(id, Type.AUDIO, outgoing, senderName, caption, attachmentUri, attachmentName, timestampMs);
    }

    public long getId() {
        return id;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    public boolean isOutgoing() {
        return outgoing;
    }

    @NonNull
    public String getSenderName() {
        return senderName;
    }

    @Nullable
    public String getText() {
        return text;
    }

    @Nullable
    public Uri getAttachmentUri() {
        return attachmentUri;
    }

    @Nullable
    public String getAttachmentName() {
        return attachmentName;
    }

    public long getTimestampMs() {
        return timestampMs;
    }
}
