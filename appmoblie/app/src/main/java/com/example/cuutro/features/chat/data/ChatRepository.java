package com.example.cuutro.features.chat.data;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cuutro.R;
import com.example.cuutro.core.network.NetworkCallExecutor;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.chat.data.model.ChatConversationItem;
import com.example.cuutro.features.chat.data.remote.ChatApiService;
import com.example.cuutro.features.chat.data.remote.ChatStompClient;
import com.example.cuutro.features.chat.data.remote.dto.ChatMessageResponseDto;
import com.example.cuutro.features.chat.data.remote.dto.ChatSendMessageRequestDto;
import com.example.cuutro.features.chat.data.remote.dto.ChatUploadFileResponseDto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ChatRepository {

    public static final String ATTACHMENT_TYPE_IMAGE = "image";
    public static final String ATTACHMENT_TYPE_VIDEO = "video";
    public static final String ATTACHMENT_TYPE_AUDIO = "audio";

    private static final String DEFAULT_UPLOAD_FOLDER = "phieu-cuu-tro/chat";
    private static final String DEFAULT_UPLOAD_FILE_NAME = "chat-media";

    private final Context appContext;
    private final ChatApiService chatApiService;
    private final AuthRepository authRepository;
    private final NetworkCallExecutor networkCallExecutor;
    private final ChatStompClient chatStompClient;
    private final Gson gson;

    public ChatRepository(
            @NonNull Context context,
            @NonNull ChatApiService chatApiService,
            @NonNull AuthRepository authRepository,
            @NonNull NetworkCallExecutor networkCallExecutor
    ) {
        this.appContext = context.getApplicationContext();
        this.chatApiService = chatApiService;
        this.authRepository = authRepository;
        this.networkCallExecutor = networkCallExecutor;
        this.chatStompClient = new ChatStompClient();
        this.gson = new GsonBuilder().create();
    }

    public interface RealtimeMessageListener {
        void onConnected();

        void onDisconnected();

        void onMessage(@NonNull ChatConversationItem message);

        void onError(@NonNull NetworkError error);
    }

    public void getConversationMessages(
            long reportId,
            @NonNull ResultCallback<List<ChatConversationItem>> callback
    ) {
        if (reportId <= 0) {
            callback.onError(new NetworkError(400, appContext.getString(R.string.chat_invalid_report_id)));
            return;
        }
        if (!authRepository.hasActiveSession()) {
            callback.onError(new NetworkError(401, appContext.getString(R.string.chat_login_required)));
            return;
        }

        networkCallExecutor.execute(
                chatApiService.getConversationMessages(reportId),
                new ResultCallback<List<ChatMessageResponseDto>>() {
                    @Override
                    public void onSuccess(List<ChatMessageResponseDto> data) {
                        if (data == null || data.isEmpty()) {
                            callback.onSuccess(Collections.emptyList());
                            return;
                        }
                        callback.onSuccess(mapConversationItems(data));
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (error.isUnauthorized()) {
                            authRepository.clearSession();
                        }
                        callback.onError(error);
                    }
                }
        );
    }

    public void sendMessage(
            long reportId,
            @NonNull SendMessageInput input,
            @NonNull ResultCallback<ChatConversationItem> callback
    ) {
        if (reportId <= 0) {
            callback.onError(new NetworkError(400, appContext.getString(R.string.chat_invalid_report_id)));
            return;
        }
        if (!authRepository.hasActiveSession()) {
            callback.onError(new NetworkError(401, appContext.getString(R.string.chat_login_required)));
            return;
        }

        String message = trimToNull(input.getMessage());
        Uri attachmentUri = input.getAttachmentUri();
        if (message == null && attachmentUri == null) {
            callback.onError(new NetworkError(400, appContext.getString(R.string.chat_message_empty)));
            return;
        }

        if (attachmentUri == null) {
            ChatSendMessageRequestDto request = new ChatSendMessageRequestDto(message, null, null, null);
            sendMessageRequest(reportId, request, callback);
            return;
        }

        uploadAttachmentThenSend(reportId, input, message, callback);
    }

    public void startRealtimeConversation(
            long reportId,
            @NonNull RealtimeMessageListener listener
    ) {
        if (reportId <= 0) {
            listener.onError(new NetworkError(400, appContext.getString(R.string.chat_invalid_report_id)));
            return;
        }
        if (!authRepository.hasActiveSession()) {
            listener.onError(new NetworkError(401, appContext.getString(R.string.chat_login_required)));
            return;
        }
        String authorizationHeader = trimToNull(authRepository.getAuthorizationHeaderValue());
        if (authorizationHeader == null) {
            listener.onError(new NetworkError(401, appContext.getString(R.string.chat_login_required)));
            return;
        }

        chatStompClient.connect(reportId, authorizationHeader, new ChatStompClient.Listener() {
            @Override
            public void onConnected() {
                listener.onConnected();
            }

            @Override
            public void onDisconnected() {
                listener.onDisconnected();
            }

            @Override
            public void onMessageBody(@NonNull String body) {
                ChatMessageResponseDto dto;
                try {
                    dto = gson.fromJson(body, ChatMessageResponseDto.class);
                } catch (Exception e) {
                    listener.onError(new NetworkError(
                            NetworkError.CODE_UNKNOWN,
                            appContext.getString(R.string.chat_realtime_payload_invalid)
                    ));
                    return;
                }
                if (dto == null) {
                    listener.onError(new NetworkError(
                            NetworkError.CODE_UNKNOWN,
                            appContext.getString(R.string.chat_realtime_payload_invalid)
                    ));
                    return;
                }
                listener.onMessage(mapConversationItem(dto));
            }

            @Override
            public void onError(@NonNull String message) {
                listener.onError(new NetworkError(
                        NetworkError.CODE_UNKNOWN,
                        appContext.getString(R.string.chat_realtime_failed, message)
                ));
            }
        });
    }

    public void stopRealtimeConversation() {
        chatStompClient.disconnect();
    }

    private void uploadAttachmentThenSend(
            long reportId,
            @NonNull SendMessageInput input,
            @Nullable String message,
            @NonNull ResultCallback<ChatConversationItem> callback
    ) {
        Uri attachmentUri = input.getAttachmentUri();
        if (attachmentUri == null) {
            callback.onError(new NetworkError(400, appContext.getString(R.string.chat_message_empty)));
            return;
        }

        MultipartBody.Part filePart;
        try {
            filePart = createAttachmentMultipartPart(
                    attachmentUri,
                    resolveAttachmentName(attachmentUri, DEFAULT_UPLOAD_FILE_NAME)
            );
        } catch (IOException exception) {
            callback.onError(new NetworkError(
                    NetworkError.CODE_UNKNOWN,
                    appContext.getString(R.string.report_attachment_file_read_failed)
            ));
            return;
        }

        RequestBody folderBody = createPlainTextRequestBody(DEFAULT_UPLOAD_FOLDER);
        RequestBody fileNameBody = createPlainTextRequestBody(
                resolveAttachmentName(attachmentUri, DEFAULT_UPLOAD_FILE_NAME)
        );

        networkCallExecutor.execute(
                chatApiService.uploadAttachment(filePart, folderBody, fileNameBody),
                new ResultCallback<ChatUploadFileResponseDto>() {
                    @Override
                    public void onSuccess(ChatUploadFileResponseDto data) {
                        Long tepTinId = data == null ? null : data.getId();
                        if (tepTinId == null || tepTinId <= 0) {
                            callback.onError(new NetworkError(
                                    NetworkError.CODE_UNKNOWN,
                                    appContext.getString(R.string.report_attachment_upload_invalid_response)
                            ));
                            return;
                        }
                        ChatSendMessageRequestDto request = new ChatSendMessageRequestDto(
                                message,
                                tepTinId,
                                null,
                                null
                        );
                        sendMessageRequest(reportId, request, callback);
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (error.isUnauthorized()) {
                            authRepository.clearSession();
                        }
                        callback.onError(error);
                    }
                }
        );
    }

    private void sendMessageRequest(
            long reportId,
            @NonNull ChatSendMessageRequestDto request,
            @NonNull ResultCallback<ChatConversationItem> callback
    ) {
        networkCallExecutor.execute(
                chatApiService.sendMessage(reportId, request),
                new ResultCallback<ChatMessageResponseDto>() {
                    @Override
                    public void onSuccess(ChatMessageResponseDto data) {
                        if (data == null) {
                            callback.onError(new NetworkError(
                                    NetworkError.CODE_UNKNOWN,
                                    appContext.getString(R.string.chat_send_failed_generic)
                            ));
                            return;
                        }
                        callback.onSuccess(mapConversationItem(data));
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (error.isUnauthorized()) {
                            authRepository.clearSession();
                        }
                        callback.onError(error);
                    }
                }
        );
    }

    @NonNull
    private List<ChatConversationItem> mapConversationItems(
            @NonNull List<ChatMessageResponseDto> messages
    ) {
        List<ChatConversationItem> mapped = new ArrayList<>();
        for (ChatMessageResponseDto dto : messages) {
            if (dto == null) {
                continue;
            }
            mapped.add(mapConversationItem(dto));
        }
        return mapped;
    }

    @NonNull
    private ChatConversationItem mapConversationItem(@NonNull ChatMessageResponseDto dto) {
        Long senderAccountId = null;
        String senderName = appContext.getString(R.string.chat_sender_rescue_team);

        ChatMessageResponseDto.SenderDto sender = dto.getSender();
        if (sender != null) {
            if (sender.getTaiKhoan() != null) {
                senderAccountId = sender.getTaiKhoan().getId();
                String username = trimToNull(sender.getTaiKhoan().getTenDangNhap());
                if (username != null) {
                    senderName = username;
                }
            }
            String fullName = trimToNull(sender.getTen());
            if (fullName != null) {
                senderName = fullName;
            }
        }

        String content = trimToNull(dto.getNoiDung());
        String mediaUrl = trimToNull(dto.getMediaUrl());
        String mediaType = trimToNull(dto.getMediaType());
        ChatConversationItem.MediaKind mediaKind = ChatConversationItem.resolveMediaKind(mediaType, mediaUrl);
        String loaiTinNhan = trimToNull(dto.getLoaiTinNhan());
        if (content == null
                && mediaKind == ChatConversationItem.MediaKind.NONE
                && loaiTinNhan != null
                && loaiTinNhan.toUpperCase(Locale.ROOT).contains("LOCATION")) {
            content = appContext.getString(R.string.chat_location_shared);
        }

        return new ChatConversationItem(
                dto.getId(),
                senderAccountId,
                senderName,
                content,
                mediaUrl,
                mediaType,
                mediaKind,
                parseCreatedAtMillis(dto.getCreatedAt())
        );
    }

    private long parseCreatedAtMillis(@Nullable String rawCreatedAt) {
        String normalized = trimToNull(rawCreatedAt);
        if (normalized == null) {
            return System.currentTimeMillis();
        }

        String prepared = normalizeIsoMillis(normalized);
        Long parsed = tryParseIsoDate(prepared, "yyyy-MM-dd'T'HH:mm:ss.SSSX");
        if (parsed != null) {
            return parsed;
        }
        parsed = tryParseIsoDate(prepared, "yyyy-MM-dd'T'HH:mm:ssX");
        if (parsed != null) {
            return parsed;
        }
        parsed = tryParseIsoDate(prepared, "yyyy-MM-dd'T'HH:mm:ss.SSS");
        if (parsed != null) {
            return parsed;
        }
        parsed = tryParseIsoDate(prepared, "yyyy-MM-dd'T'HH:mm:ss");
        if (parsed != null) {
            return parsed;
        }

        return System.currentTimeMillis();
    }

    @Nullable
    private Long tryParseIsoDate(@NonNull String raw, @NonNull String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
            sdf.setLenient(true);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date parsedDate = sdf.parse(raw);
            return parsedDate == null ? null : parsedDate.getTime();
        } catch (ParseException ignored) {
            return null;
        }
    }

    @NonNull
    private String normalizeIsoMillis(@NonNull String raw) {
        int tIndex = raw.indexOf('T');
        int dotIndex = raw.indexOf('.');
        if (tIndex < 0 || dotIndex < 0 || dotIndex < tIndex) {
            return raw;
        }

        int zoneStart = findZoneStart(raw, dotIndex + 1);
        if (zoneStart < 0) {
            zoneStart = raw.length();
        }

        String fraction = raw.substring(dotIndex + 1, zoneStart);
        if (fraction.isEmpty()) {
            return raw;
        }

        if (fraction.length() > 3) {
            fraction = fraction.substring(0, 3);
        }
        while (fraction.length() < 3) {
            fraction = fraction + "0";
        }

        return raw.substring(0, dotIndex + 1)
                + fraction
                + raw.substring(zoneStart);
    }

    private int findZoneStart(@NonNull String value, int fromIndex) {
        for (int i = fromIndex; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == 'Z' || ch == '+' || ch == '-') {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    private MultipartBody.Part createAttachmentMultipartPart(
            @NonNull Uri uri,
            @NonNull String fileName
    ) throws IOException {
        ContentResolver resolver = appContext.getContentResolver();
        String mimeType = trimToNull(resolver.getType(uri));
        MediaType mediaType = MediaType.parse(mimeType != null ? mimeType : "application/octet-stream");
        RequestBody requestBody = new UriRequestBody(resolver, uri, mediaType);
        return MultipartBody.Part.createFormData("tepTin", fileName, requestBody);
    }

    @NonNull
    private RequestBody createPlainTextRequestBody(@Nullable String value) {
        String safeValue = value == null ? "" : value;
        return new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return MultipartBody.FORM;
            }

            @Override
            public long contentLength() {
                return safeValue.getBytes(StandardCharsets.UTF_8).length;
            }

            @Override
            public void writeTo(@NonNull BufferedSink sink) throws IOException {
                sink.writeUtf8(safeValue);
            }
        };
    }

    @NonNull
    private String resolveAttachmentName(@NonNull Uri uri, @NonNull String fallback) {
        Cursor cursor = null;
        try {
            cursor = appContext.getContentResolver().query(
                    uri,
                    new String[] {OpenableColumns.DISPLAY_NAME},
                    null,
                    null,
                    null
            );
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String name = trimToNull(cursor.getString(index));
                    if (name != null) {
                        return name;
                    }
                }
            }
        } catch (SecurityException ignored) {
            // Ignore and fallback.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        String lastPath = trimToNull(uri.getLastPathSegment());
        if (lastPath != null) {
            return lastPath;
        }
        return fallback;
    }

    @Nullable
    private String trimToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public static final class SendMessageInput {

        @Nullable
        private final String message;

        @Nullable
        private final Uri attachmentUri;

        @Nullable
        private final String attachmentType;

        public SendMessageInput(
                @Nullable String message,
                @Nullable Uri attachmentUri,
                @Nullable String attachmentType
        ) {
            this.message = message;
            this.attachmentUri = attachmentUri;
            this.attachmentType = attachmentType;
        }

        @Nullable
        public String getMessage() {
            return message;
        }

        @Nullable
        public Uri getAttachmentUri() {
            return attachmentUri;
        }

        @Nullable
        public String getAttachmentType() {
            return attachmentType;
        }
    }

    private static final class UriRequestBody extends RequestBody {

        @NonNull
        private final ContentResolver resolver;

        @NonNull
        private final Uri uri;

        @Nullable
        private final MediaType contentType;

        private UriRequestBody(
                @NonNull ContentResolver resolver,
                @NonNull Uri uri,
                @Nullable MediaType contentType
        ) {
            this.resolver = resolver;
            this.uri = uri;
            this.contentType = contentType;
        }

        @Nullable
        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            AssetFileDescriptor descriptor = null;
            try {
                descriptor = resolver.openAssetFileDescriptor(uri, "r");
                if (descriptor == null) {
                    return -1L;
                }
                return descriptor.getLength();
            } catch (Exception ignored) {
                return -1L;
            } finally {
                if (descriptor != null) {
                    try {
                        descriptor.close();
                    } catch (Exception ignored) {
                        // Ignore.
                    }
                }
            }
        }

        @Override
        public void writeTo(@NonNull BufferedSink sink) throws IOException {
            InputStream inputStream = resolver.openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("Cannot open input stream for uri: " + uri);
            }
            try (InputStream stream = inputStream) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    sink.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
