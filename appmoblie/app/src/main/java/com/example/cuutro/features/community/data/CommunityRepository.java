package com.example.cuutro.features.community.data;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cuutro.R;
import com.example.cuutro.core.network.BackendConfig;
import com.example.cuutro.core.network.NetworkCallExecutor;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.community.data.remote.CommunityApiService;
import com.example.cuutro.features.community.data.remote.dto.BangTinCreateRequestDto;
import com.example.cuutro.features.community.data.remote.dto.BangTinItemDto;
import com.example.cuutro.features.community.model.CommunityPostItem;
import com.example.cuutro.features.report.data.remote.dto.TepTinUploadResponseDto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class CommunityRepository {

    private static final String DEFAULT_POST_ID_PREFIX = "community_";
    private static final String UPLOAD_FOLDER_COMMUNITY = "bang-tin";
    private static final String DEFAULT_UPLOAD_FILE_NAME = "bang-tin-media";

    private final Context appContext;
    private final CommunityApiService communityApiService;
    private final AuthRepository authRepository;
    private final NetworkCallExecutor networkCallExecutor;

    public CommunityRepository(
            @NonNull Context context,
            @NonNull CommunityApiService communityApiService,
            @NonNull AuthRepository authRepository,
            @NonNull NetworkCallExecutor networkCallExecutor
    ) {
        this.appContext = context.getApplicationContext();
        this.communityApiService = communityApiService;
        this.authRepository = authRepository;
        this.networkCallExecutor = networkCallExecutor;
    }

    public void getPublicPosts(@NonNull ResultCallback<List<CommunityPostItem>> callback) {
        getPublicPosts(callback, true);
    }

    public void createPost(
            @NonNull CreateCommunityPostInput input,
            @NonNull ResultCallback<CommunityPostItem> callback
    ) {
        if (!authRepository.hasActiveSession()) {
            callback.onError(new NetworkError(
                    401,
                    appContext.getString(R.string.auth_required_create_post_message)
            ));
            return;
        }

        String tieuDe = trimToNull(input.getTieuDe());
        String noiDung = trimToNull(input.getNoiDung());
        if (tieuDe == null) {
            callback.onError(new NetworkError(400, appContext.getString(R.string.community_post_title_required)));
            return;
        }
        if (noiDung == null) {
            callback.onError(new NetworkError(400, appContext.getString(R.string.community_post_description_required)));
            return;
        }

        String diaChi = trimToNull(input.getDiaChi());
        Uri mediaUri = input.getMediaUri();
        if (mediaUri == null) {
            submitCreatePost(tieuDe, noiDung, diaChi, null, callback);
            return;
        }
        uploadMediaThenCreatePost(tieuDe, noiDung, diaChi, mediaUri, callback);
    }

    private void getPublicPosts(
            @NonNull ResultCallback<List<CommunityPostItem>> callback,
            boolean canRetryUnauthorized
    ) {
        networkCallExecutor.execute(
                communityApiService.getPublicPosts(),
                new ResultCallback<List<BangTinItemDto>>() {
                    @Override
                    public void onSuccess(List<BangTinItemDto> data) {
                        callback.onSuccess(mapPostItems(data));
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (error.isUnauthorized() && canRetryUnauthorized) {
                            authRepository.clearSession();
                            getPublicPosts(callback, false);
                            return;
                        }
                        callback.onError(error);
                    }
                }
        );
    }

    private void uploadMediaThenCreatePost(
            @NonNull String tieuDe,
            @NonNull String noiDung,
            @Nullable String diaChi,
            @NonNull Uri mediaUri,
            @NonNull ResultCallback<CommunityPostItem> callback
    ) {
        MultipartBody.Part filePart;
        try {
            filePart = createAttachmentMultipartPart(mediaUri);
        } catch (IOException exception) {
            callback.onError(new NetworkError(
                    NetworkError.CODE_UNKNOWN,
                    appContext.getString(R.string.report_attachment_file_read_failed)
            ));
            return;
        }

        RequestBody folderBody = createPlainTextRequestBody(UPLOAD_FOLDER_COMMUNITY);
        RequestBody fileNameBody = createPlainTextRequestBody(
                resolveAttachmentName(mediaUri, DEFAULT_UPLOAD_FILE_NAME)
        );

        networkCallExecutor.execute(
                communityApiService.uploadAttachment(filePart, folderBody, fileNameBody),
                new ResultCallback<TepTinUploadResponseDto>() {
                    @Override
                    public void onSuccess(TepTinUploadResponseDto data) {
                        Long tepTinId = data != null ? data.getId() : null;
                        if (tepTinId == null || tepTinId <= 0L) {
                            callback.onError(new NetworkError(
                                    NetworkError.CODE_UNKNOWN,
                                    appContext.getString(R.string.report_attachment_upload_invalid_response)
                            ));
                            return;
                        }
                        submitCreatePost(tieuDe, noiDung, diaChi, tepTinId, callback);
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

    private void submitCreatePost(
            @NonNull String tieuDe,
            @NonNull String noiDung,
            @Nullable String diaChi,
            @Nullable Long tepTinId,
            @NonNull ResultCallback<CommunityPostItem> callback
    ) {
        BangTinCreateRequestDto.ViTriInputDto viTri = diaChi == null
                ? null
                : new BangTinCreateRequestDto.ViTriInputDto(diaChi, null, null);
        BangTinCreateRequestDto request = new BangTinCreateRequestDto(tieuDe, noiDung, tepTinId, viTri);

        networkCallExecutor.execute(
                communityApiService.createPost(request),
                new ResultCallback<BangTinItemDto>() {
                    @Override
                    public void onSuccess(BangTinItemDto data) {
                        callback.onSuccess(mapPostItem(data, 0));
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
    private List<CommunityPostItem> mapPostItems(@Nullable List<BangTinItemDto> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        List<CommunityPostItem> mapped = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            BangTinItemDto item = items.get(i);
            if (item == null) {
                continue;
            }
            mapped.add(mapPostItem(item, i));
        }
        return mapped;
    }

    @NonNull
    private CommunityPostItem mapPostItem(@Nullable BangTinItemDto item, int position) {
        String id = resolveId(item, position);
        String authorName = resolveAuthorName(item);
        String title = resolveTitle(item);
        String body = resolveBody(item);
        String location = resolveLocation(item);
        String date = resolveCreatedAt(item == null ? null : item.getCreatedAt());
        String postImageUrl = resolveAbsoluteUrl(item != null && item.getTepTin() != null
                ? item.getTepTin().getDuongDan()
                : null);
        String avatarUrl = resolveAbsoluteUrl(item != null && item.getNguoiDung() != null
                ? item.getNguoiDung().getAvatarUrl()
                : null);
        String mediaCounter = postImageUrl == null
                ? ""
                : appContext.getString(R.string.community_post_media_counter_1_1);

        return new CommunityPostItem(
                id,
                R.drawable.community_post_avatar_joshua,
                avatarUrl,
                authorName,
                false,
                location,
                R.drawable.community_post_meals_round,
                postImageUrl,
                mediaCounter,
                appContext.getString(R.string.community_post_public_visibility),
                authorName + " " + title + (body.isEmpty() ? "" : " - " + body),
                date
        );
    }

    @NonNull
    private String resolveId(@Nullable BangTinItemDto item, int position) {
        if (item == null || item.getId() == null) {
            return DEFAULT_POST_ID_PREFIX + position;
        }
        return String.valueOf(item.getId());
    }

    @NonNull
    private String resolveAuthorName(@Nullable BangTinItemDto item) {
        if (item == null || item.getNguoiDung() == null) {
            return appContext.getString(R.string.community_post_author_unknown);
        }
        String value = trimToNull(item.getNguoiDung().getTen());
        if (value != null) {
            return value;
        }
        return appContext.getString(R.string.community_post_author_unknown);
    }

    @NonNull
    private String resolveTitle(@Nullable BangTinItemDto item) {
        String value = item == null ? null : trimToNull(item.getTieuDe());
        if (value != null) {
            return value;
        }
        return appContext.getString(R.string.community_post_title_fallback);
    }

    @NonNull
    private String resolveBody(@Nullable BangTinItemDto item) {
        String value = item == null ? null : trimToNull(item.getNoiDung());
        return value == null ? "" : value;
    }

    @NonNull
    private String resolveLocation(@Nullable BangTinItemDto item) {
        if (item == null || item.getViTri() == null) {
            return appContext.getString(R.string.community_post_location_unknown);
        }
        String value = trimToNull(item.getViTri().getDiaChi());
        if (value != null) {
            return value;
        }
        return appContext.getString(R.string.community_post_location_unknown);
    }

    @NonNull
    private String resolveCreatedAt(@Nullable String rawCreatedAt) {
        String normalized = trimToNull(rawCreatedAt);
        if (normalized == null) {
            return appContext.getString(R.string.community_post_date_recent);
        }
        if (normalized.length() >= 10
                && normalized.charAt(4) == '-'
                && normalized.charAt(7) == '-') {
            String year = normalized.substring(0, 4);
            String month = normalized.substring(5, 7);
            String day = normalized.substring(8, 10);
            return day + "/" + month + "/" + year;
        }
        return normalized;
    }

    @Nullable
    private String resolveAbsoluteUrl(@Nullable String rawUrl) {
        String normalized = trimToNull(rawUrl);
        if (normalized == null) {
            return null;
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return normalized;
        }
        String base = BackendConfig.getBaseUrl();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return base + normalized;
    }

    @Nullable
    private String trimToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    @NonNull
    private MultipartBody.Part createAttachmentMultipartPart(@NonNull Uri uri) throws IOException {
        ContentResolver resolver = appContext.getContentResolver();
        String mimeType = trimToNull(resolver.getType(uri));
        MediaType mediaType = MediaType.parse(mimeType != null ? mimeType : "application/octet-stream");
        String fileName = resolveAttachmentName(uri, DEFAULT_UPLOAD_FILE_NAME);
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
                    new String[]{OpenableColumns.DISPLAY_NAME},
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

    public static class CreateCommunityPostInput {

        private final String tieuDe;
        private final String noiDung;
        private final String diaChi;
        private final Uri mediaUri;

        public CreateCommunityPostInput(
                @NonNull String tieuDe,
                @NonNull String noiDung,
                @Nullable String diaChi,
                @Nullable Uri mediaUri
        ) {
            this.tieuDe = tieuDe;
            this.noiDung = noiDung;
            this.diaChi = diaChi;
            this.mediaUri = mediaUri;
        }

        @NonNull
        public String getTieuDe() {
            return tieuDe;
        }

        @NonNull
        public String getNoiDung() {
            return noiDung;
        }

        @Nullable
        public String getDiaChi() {
            return diaChi;
        }

        @Nullable
        public Uri getMediaUri() {
            return mediaUri;
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
            } catch (IOException | SecurityException ignored) {
                return -1L;
            } finally {
                if (descriptor != null) {
                    try {
                        descriptor.close();
                    } catch (IOException ignored) {
                        // Ignore.
                    }
                }
            }
        }

        @Override
        public void writeTo(@NonNull BufferedSink sink) throws IOException {
            InputStream inputStream;
            try {
                inputStream = resolver.openInputStream(uri);
            } catch (SecurityException exception) {
                throw new IOException("Cannot open attachment stream", exception);
            }
            if (inputStream == null) {
                throw new IOException("Attachment stream is null");
            }

            byte[] buffer = new byte[8192];
            int bytesRead;
            try (InputStream stream = inputStream) {
                while ((bytesRead = stream.read(buffer)) != -1) {
                    sink.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
