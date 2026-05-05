package com.example.cuutro.features.chat.ui.controller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuutro.R;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.chat.data.ChatRepository;
import com.example.cuutro.features.chat.data.model.ChatConversationItem;
import com.example.cuutro.features.chat.ui.ChatMessage;
import com.example.cuutro.features.chat.ui.ChatsAdapter;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatsController {

    private static final long REALTIME_RECONNECT_DELAY_MS = 3000L;

    private final AppCompatActivity activity;
    private final long reportId;
    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;
    private final Handler realtimeHandler = new Handler(Looper.getMainLooper());
    private final Runnable realtimeReconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (realtimeStarted && !activity.isFinishing() && !activity.isDestroyed()) {
                openRealtimeConnection();
            }
        }
    };

    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private long localMessageId = 1L;

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ImageButton pickImageButton;
    private ImageButton pickVideoButton;
    private ImageButton recordAudioButton;
    private TextView chatSubtitleText;
    private TextView recordingStatusText;

    private HorizontalScrollView composerMediaScroll;
    private LinearLayout composerMediaContainer;
    private LinearLayout composerAudioContainer;
    private ImageButton composerAudioPlayButton;
    private TextView composerAudioNameText;
    private TextView composerAudioRemoveText;

    private ChatsAdapter chatsAdapter;

    private ActivityResultLauncher<String[]> pickImageLauncher;
    private ActivityResultLauncher<String[]> pickVideoLauncher;
    private ActivityResultLauncher<String> recordAudioPermissionLauncher;

    @Nullable
    private Uri selectedImageUri;
    @Nullable
    private Uri selectedVideoUri;
    @Nullable
    private Uri selectedAudioUri;
    @Nullable
    private String selectedAudioName;

    @Nullable
    private MediaRecorder audioRecorder;
    @Nullable
    private File recordingOutputFile;
    private boolean recordingAudio;

    @Nullable
    private MediaPlayer composerAudioPlayer;
    private boolean composerAudioReady;
    private boolean composerAudioPlaying;

    @Nullable
    private MediaPlayer messageAudioPlayer;
    @Nullable
    private Long playingAudioMessageId;
    private boolean messageAudioReady;
    private boolean messageAudioPlaying;

    private boolean loadingMessages;
    private boolean sendingMessage;
    private boolean realtimeStarted;
    private long lastRealtimeErrorToastAtMs;

    public ChatsController(
            @NonNull AppCompatActivity activity,
            long reportId,
            @NonNull ChatRepository chatRepository,
            @NonNull AuthRepository authRepository
    ) {
        this.activity = activity;
        this.reportId = reportId;
        this.chatRepository = chatRepository;
        this.authRepository = authRepository;

        bindViews();
        setupActivityResultLaunchers();
        setupRecyclerView();
        setupActions();
        renderComposerAttachments();
        loadConversation();
    }

    public void onPause() {
        stopRealtime();
        pauseComposerAudioPlayback();
        pauseMessageAudioPlayback();
        if (recordingAudio) {
            stopAudioRecording(false);
        }
    }

    public void onResume() {
        loadConversation(false);
        startRealtime();
    }

    public void onDestroy() {
        stopRealtime();
        releaseComposerAudioPlayer();
        releaseMessageAudioPlayer();
        releaseAudioRecorder();
    }

    private void bindViews() {
        chatRecyclerView = activity.findViewById(R.id.rvChatMessages);
        messageEditText = activity.findViewById(R.id.edtChatMessage);
        sendButton = activity.findViewById(R.id.btnChatSend);
        pickImageButton = activity.findViewById(R.id.btnChatPickImage);
        pickVideoButton = activity.findViewById(R.id.btnChatPickVideo);
        recordAudioButton = activity.findViewById(R.id.btnChatRecordAudio);
        chatSubtitleText = activity.findViewById(R.id.tvChatSubtitle);
        recordingStatusText = activity.findViewById(R.id.tvChatRecordingStatus);

        composerMediaScroll = activity.findViewById(R.id.scrollChatComposerMedia);
        composerMediaContainer = activity.findViewById(R.id.layoutChatComposerMedia);
        composerAudioContainer = activity.findViewById(R.id.layoutChatComposerAudio);
        composerAudioPlayButton = activity.findViewById(R.id.btnChatComposerAudioPlay);
        composerAudioNameText = activity.findViewById(R.id.tvChatComposerAudioName);
        composerAudioRemoveText = activity.findViewById(R.id.tvChatComposerAudioRemove);
    }

    private void setupActivityResultLaunchers() {
        pickImageLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::onImagePicked
        );
        pickVideoLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::onVideoPicked
        );
        recordAudioPermissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                this::onRecordAudioPermissionResult
        );
    }

    private void setupRecyclerView() {
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(activity);
        chatsAdapter = new ChatsAdapter(
                LayoutInflater.from(activity),
                timeFormat,
                new ChatsAdapter.MessageActionListener() {
                    @Override
                    public void onAudioClicked(@NonNull ChatMessage message) {
                        toggleMessageAudioPlayback(message);
                    }

                    @Override
                    public void onMediaClicked(@NonNull ChatMessage message) {
                        openMediaAttachment(message);
                    }

                    @Override
                    public boolean isMessageAudioPlaying(@NonNull ChatMessage message) {
                        return playingAudioMessageId != null
                                && playingAudioMessageId == message.getId()
                                && messageAudioPlaying;
                    }
                }
        );

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatsAdapter);
        chatsAdapter.setMessages(chatMessages);
    }

    private void setupActions() {
        ImageButton backButton = activity.findViewById(R.id.btnChatBack);

        if (backButton != null) {
            backButton.setOnClickListener(v -> activity.finish());
        }
        if (pickImageButton != null) {
            pickImageButton.setOnClickListener(v -> pickImageLauncher.launch(new String[] {"image/*"}));
        }
        if (pickVideoButton != null) {
            pickVideoButton.setOnClickListener(v -> pickVideoLauncher.launch(new String[] {"video/*"}));
        }
        if (recordAudioButton != null) {
            recordAudioButton.setOnClickListener(v -> toggleAudioRecording());
        }
        if (sendButton != null) {
            sendButton.setOnClickListener(v -> sendCurrentMessage());
        }
        if (composerAudioPlayButton != null) {
            composerAudioPlayButton.setOnClickListener(v -> toggleComposerAudioPlayback());
        }
        if (composerAudioRemoveText != null) {
            composerAudioRemoveText.setOnClickListener(v -> removeComposerAudioAttachment());
        }
        if (messageEditText != null) {
            messageEditText.setOnEditorActionListener((v, actionId, event) -> {
                sendCurrentMessage();
                return true;
            });
        }
    }

    private void loadConversation() {
        loadConversation(true);
    }

    private void loadConversation(boolean showErrorToast) {
        if (loadingMessages) {
            return;
        }
        if (!authRepository.hasActiveSession()) {
            Toast.makeText(activity, R.string.chat_login_required, Toast.LENGTH_SHORT).show();
            activity.finish();
            return;
        }

        loadingMessages = true;
        updateSubtitleText(activity.getString(R.string.chat_loading_messages));

        chatRepository.getConversationMessages(reportId, new ResultCallback<List<ChatConversationItem>>() {
            @Override
            public void onSuccess(List<ChatConversationItem> data) {
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
                loadingMessages = false;
                chatMessages.clear();
                if (data != null) {
                    for (ChatConversationItem item : data) {
                        if (item == null) {
                            continue;
                        }
                        chatMessages.add(mapConversationItemToUi(item));
                    }
                }
                chatsAdapter.setMessages(chatMessages);
                scrollToBottom();
                updateSubtitleText(chatMessages.isEmpty()
                        ? activity.getString(R.string.chat_empty_state)
                        : activity.getString(R.string.chat_subtitle));
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
                loadingMessages = false;
                updateSubtitleText(activity.getString(R.string.chat_subtitle));
                if (showErrorToast) {
                    Toast.makeText(
                            activity,
                            activity.getString(R.string.chat_load_failed, error.getMessage()),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

    private void startRealtime() {
        if (realtimeStarted) {
            return;
        }
        realtimeStarted = true;
        openRealtimeConnection();
    }

    private void openRealtimeConnection() {
        cancelRealtimeReconnect();
        chatRepository.startRealtimeConversation(reportId, new ChatRepository.RealtimeMessageListener() {
            @Override
            public void onConnected() {
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
                updateSubtitleText(chatMessages.isEmpty()
                        ? activity.getString(R.string.chat_empty_state)
                        : activity.getString(R.string.chat_subtitle));
            }

            @Override
            public void onDisconnected() {
                scheduleRealtimeReconnect();
            }

            @Override
            public void onMessage(@NonNull ChatConversationItem message) {
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
                upsertMessage(mapConversationItemToUi(message));
                updateSubtitleText(activity.getString(R.string.chat_subtitle));
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
                long now = System.currentTimeMillis();
                if (now - lastRealtimeErrorToastAtMs >= 10000L) {
                    lastRealtimeErrorToastAtMs = now;
                    Toast.makeText(activity, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void stopRealtime() {
        realtimeStarted = false;
        cancelRealtimeReconnect();
        chatRepository.stopRealtimeConversation();
    }

    private void scheduleRealtimeReconnect() {
        if (!realtimeStarted) {
            return;
        }
        realtimeHandler.removeCallbacks(realtimeReconnectRunnable);
        realtimeHandler.postDelayed(realtimeReconnectRunnable, REALTIME_RECONNECT_DELAY_MS);
    }

    private void cancelRealtimeReconnect() {
        realtimeHandler.removeCallbacks(realtimeReconnectRunnable);
    }

    private ChatMessage mapConversationItemToUi(@NonNull ChatConversationItem item) {
        boolean outgoing = item.isOutgoing(authRepository.getCurrentTaiKhoanId());
        long messageId = item.getId() != null ? item.getId() : nextMessageId();
        long timestampMs = item.getCreatedAtMillis() > 0
                ? item.getCreatedAtMillis()
                : System.currentTimeMillis();

        String senderName = trimToNull(item.getSenderName());
        if (senderName == null) {
            senderName = outgoing
                    ? activity.getString(R.string.chat_sender_you)
                    : activity.getString(R.string.chat_sender_rescue_team);
        }

        String text = trimToNull(item.getContent());
        Uri attachmentUri = null;
        if (!isBlank(item.getMediaUrl())) {
            attachmentUri = Uri.parse(item.getMediaUrl().trim());
        }

        ChatConversationItem.MediaKind mediaKind = item.getMediaKind();
        if (attachmentUri != null && mediaKind == ChatConversationItem.MediaKind.VIDEO) {
            return ChatMessage.video(messageId, outgoing, senderName, text, attachmentUri, timestampMs);
        }
        if (attachmentUri != null && mediaKind == ChatConversationItem.MediaKind.AUDIO) {
            String audioName = resolveAttachmentName(
                    attachmentUri,
                    activity.getString(R.string.report_attachment_audio_default_name)
            );
            return ChatMessage.audio(messageId, outgoing, senderName, text, attachmentUri, audioName, timestampMs);
        }
        if (attachmentUri != null && mediaKind == ChatConversationItem.MediaKind.IMAGE) {
            return ChatMessage.image(messageId, outgoing, senderName, text, attachmentUri, timestampMs);
        }

        return ChatMessage.text(
                messageId,
                outgoing,
                senderName,
                text == null ? "" : text,
                timestampMs
        );
    }

    private void onImagePicked(@Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        persistReadPermission(uri);
        selectedImageUri = uri;
        selectedVideoUri = null;
        selectedAudioUri = null;
        selectedAudioName = null;
        releaseComposerAudioPlayer();
        renderComposerAttachments();
    }

    private void onVideoPicked(@Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        persistReadPermission(uri);
        selectedVideoUri = uri;
        selectedImageUri = null;
        selectedAudioUri = null;
        selectedAudioName = null;
        releaseComposerAudioPlayer();
        renderComposerAttachments();
    }

    private void onRecordAudioPermissionResult(boolean granted) {
        if (!granted) {
            Toast.makeText(activity, R.string.chat_record_audio_permission_denied, Toast.LENGTH_SHORT).show();
            return;
        }
        startAudioRecording();
    }

    private void toggleAudioRecording() {
        if (recordingAudio) {
            stopAudioRecording(true);
            return;
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startAudioRecording();
            return;
        }
        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
    }

    private void startAudioRecording() {
        File outputFile = createAudioRecordingFile();
        if (outputFile == null) {
            Toast.makeText(activity, R.string.chat_record_audio_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        releaseAudioRecorder();
        MediaRecorder recorder = new MediaRecorder();
        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(96000);
            recorder.setAudioSamplingRate(44100);
            recorder.setOutputFile(outputFile.getAbsolutePath());
            recorder.prepare();
            recorder.start();
        } catch (IOException | RuntimeException e) {
            recorder.release();
            if (outputFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                outputFile.delete();
            }
            Toast.makeText(activity, R.string.chat_record_audio_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        selectedImageUri = null;
        selectedVideoUri = null;
        removeComposerAudioAttachment();

        audioRecorder = recorder;
        recordingOutputFile = outputFile;
        recordingAudio = true;
        updateRecordingUiState();
        renderComposerAttachments();
    }

    private void stopAudioRecording(boolean attachToComposer) {
        MediaRecorder recorder = audioRecorder;
        File outputFile = recordingOutputFile;
        recordingAudio = false;
        audioRecorder = null;
        recordingOutputFile = null;

        boolean recordedSuccessfully = false;
        if (recorder != null) {
            try {
                recorder.stop();
                recordedSuccessfully = true;
            } catch (RuntimeException ignored) {
                recordedSuccessfully = false;
            }
            recorder.reset();
            recorder.release();
        }

        if (!attachToComposer || outputFile == null || !recordedSuccessfully || !outputFile.exists()) {
            if (outputFile != null && outputFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                outputFile.delete();
            }
            updateRecordingUiState();
            renderComposerAttachments();
            return;
        }

        selectedAudioUri = Uri.fromFile(outputFile);
        selectedAudioName = outputFile.getName();
        Toast.makeText(activity, R.string.chat_record_audio_done, Toast.LENGTH_SHORT).show();
        updateRecordingUiState();
        renderComposerAttachments();
    }

    private void updateRecordingUiState() {
        if (recordAudioButton != null) {
            int tint = ContextCompat.getColor(
                    activity,
                    recordingAudio ? R.color.white : R.color.report_action_red
            );
            recordAudioButton.setImageTintList(ColorStateList.valueOf(tint));
        }
        if (recordingStatusText != null) {
            recordingStatusText.setVisibility(recordingAudio ? View.VISIBLE : View.GONE);
        }
    }

    @Nullable
    private File createAudioRecordingFile() {
        File parent = new File(activity.getCacheDir(), "chat-audio");
        if (!parent.exists() && !parent.mkdirs()) {
            return null;
        }
        String fileName = String.format(
                Locale.US,
                "chat_audio_%d.m4a",
                System.currentTimeMillis()
        );
        return new File(parent, fileName);
    }

    private void renderComposerAttachments() {
        renderComposerMediaPreview();
        renderComposerAudioPreview();
    }

    private void renderComposerMediaPreview() {
        if (composerMediaContainer == null || composerMediaScroll == null) {
            return;
        }
        composerMediaContainer.removeAllViews();

        if (selectedImageUri != null) {
            addComposerMediaItem(selectedImageUri, false);
        } else if (selectedVideoUri != null) {
            addComposerMediaItem(selectedVideoUri, true);
        }

        composerMediaScroll.setVisibility(
                composerMediaContainer.getChildCount() == 0 ? View.GONE : View.VISIBLE
        );
    }

    private void addComposerMediaItem(@NonNull Uri uri, boolean isVideo) {
        if (composerMediaContainer == null) {
            return;
        }

        View itemView = LayoutInflater.from(activity).inflate(
                R.layout.item_report_attachment_media,
                composerMediaContainer,
                false
        );

        ImageView previewImage = itemView.findViewById(R.id.ivReportAttachmentPreview);
        ImageView videoOverlay = itemView.findViewById(R.id.ivReportAttachmentVideoOverlay);
        ImageButton removeButton = itemView.findViewById(R.id.btnReportAttachmentRemove);

        if (isVideo) {
            previewImage.setImageResource(R.drawable.ic_report_video);
            previewImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            previewImage.setImageTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.report_action_red))
            );
            videoOverlay.setVisibility(View.VISIBLE);
            removeButton.setOnClickListener(v -> {
                selectedVideoUri = null;
                renderComposerAttachments();
            });
        } else {
            previewImage.setImageTintList(null);
            previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            previewImage.setImageURI(uri);
            videoOverlay.setVisibility(View.GONE);
            removeButton.setOnClickListener(v -> {
                selectedImageUri = null;
                renderComposerAttachments();
            });
        }

        composerMediaContainer.addView(itemView);
    }

    private void renderComposerAudioPreview() {
        if (composerAudioContainer == null) {
            return;
        }
        if (selectedAudioUri == null) {
            composerAudioContainer.setVisibility(View.GONE);
            updateComposerAudioPlaybackButton();
            return;
        }

        composerAudioContainer.setVisibility(View.VISIBLE);
        String fallback = activity.getString(R.string.report_attachment_audio_default_name);
        String audioName = selectedAudioName;
        if (isBlank(audioName)) {
            audioName = resolveAttachmentName(selectedAudioUri, fallback);
            selectedAudioName = audioName;
        }
        composerAudioNameText.setText(audioName);
        updateComposerAudioPlaybackButton();
    }

    private void removeComposerAudioAttachment() {
        selectedAudioUri = null;
        selectedAudioName = null;
        releaseComposerAudioPlayer();
        renderComposerAttachments();
    }

    private void toggleComposerAudioPlayback() {
        if (selectedAudioUri == null) {
            return;
        }
        if (composerAudioPlayer != null && composerAudioReady) {
            if (composerAudioPlaying) {
                pauseComposerAudioPlayback();
            } else {
                try {
                    composerAudioPlayer.start();
                    composerAudioPlaying = true;
                    updateComposerAudioPlaybackButton();
                } catch (IllegalStateException ignored) {
                    releaseComposerAudioPlayer();
                }
            }
            return;
        }
        startComposerAudioPlayback(selectedAudioUri);
    }

    private void startComposerAudioPlayback(@NonNull Uri audioUri) {
        releaseComposerAudioPlayer();
        MediaPlayer player = new MediaPlayer();
        composerAudioPlayer = player;
        composerAudioReady = false;
        composerAudioPlaying = false;
        updateComposerAudioPlaybackButton();

        player.setOnPreparedListener(mp -> {
            composerAudioReady = true;
            try {
                mp.start();
                composerAudioPlaying = true;
            } catch (IllegalStateException ignored) {
                releaseComposerAudioPlayer();
                return;
            }
            updateComposerAudioPlaybackButton();
        });
        player.setOnCompletionListener(mp -> {
            composerAudioPlaying = false;
            if (composerAudioReady) {
                try {
                    mp.seekTo(0);
                } catch (IllegalStateException ignored) {
                    // Ignore.
                }
            }
            updateComposerAudioPlaybackButton();
        });
        player.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(activity, R.string.report_attachment_audio_play_failed, Toast.LENGTH_SHORT).show();
            releaseComposerAudioPlayer();
            return true;
        });

        try {
            player.setDataSource(activity, audioUri);
            player.prepareAsync();
        } catch (IOException | SecurityException | IllegalStateException e) {
            Toast.makeText(activity, R.string.report_attachment_audio_play_failed, Toast.LENGTH_SHORT).show();
            releaseComposerAudioPlayer();
        }
    }

    private void pauseComposerAudioPlayback() {
        if (composerAudioPlayer == null || !composerAudioReady) {
            return;
        }
        try {
            composerAudioPlayer.pause();
            composerAudioPlaying = false;
            updateComposerAudioPlaybackButton();
        } catch (IllegalStateException ignored) {
            releaseComposerAudioPlayer();
        }
    }

    private void updateComposerAudioPlaybackButton() {
        if (composerAudioPlayButton == null) {
            return;
        }
        composerAudioPlayButton.setImageResource(
                composerAudioPlaying ? R.drawable.ic_report_audio_pause : R.drawable.ic_report_audio_play
        );
        composerAudioPlayButton.setContentDescription(
                activity.getString(
                        composerAudioPlaying
                                ? R.string.report_attachment_audio_pause
                                : R.string.report_attachment_audio_play
                )
        );
    }

    private void releaseComposerAudioPlayer() {
        if (composerAudioPlayer != null) {
            try {
                if (composerAudioReady) {
                    composerAudioPlayer.stop();
                }
            } catch (IllegalStateException ignored) {
                // Ignore.
            }
            try {
                composerAudioPlayer.reset();
            } catch (IllegalStateException ignored) {
                // Ignore.
            }
            composerAudioPlayer.release();
            composerAudioPlayer = null;
        }
        composerAudioReady = false;
        composerAudioPlaying = false;
        updateComposerAudioPlaybackButton();
    }

    private void sendCurrentMessage() {
        if (sendingMessage || loadingMessages) {
            return;
        }
        if (recordingAudio) {
            stopAudioRecording(true);
        }

        String inputText = messageEditText != null
                ? messageEditText.getText().toString().trim()
                : "";

        Uri attachmentUri = resolveSelectedAttachmentUri();
        String attachmentType = resolveSelectedAttachmentType();

        if (inputText.isEmpty() && attachmentUri == null) {
            Toast.makeText(activity, R.string.chat_message_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        setSendingState(true);
        ChatRepository.SendMessageInput sendInput = new ChatRepository.SendMessageInput(
                inputText,
                attachmentUri,
                attachmentType
        );

        chatRepository.sendMessage(reportId, sendInput, new ResultCallback<ChatConversationItem>() {
            @Override
            public void onSuccess(ChatConversationItem data) {
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
                setSendingState(false);
                if (data == null) {
                    Toast.makeText(activity, R.string.chat_send_failed_generic, Toast.LENGTH_SHORT).show();
                    return;
                }
                ChatMessage uiMessage = mapConversationItemToUi(data);
                upsertMessage(uiMessage);
                clearComposerAfterSend();
                updateSubtitleText(activity.getString(R.string.chat_subtitle));
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
                setSendingState(false);
                Toast.makeText(
                        activity,
                        activity.getString(R.string.chat_send_failed, error.getMessage()),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void clearComposerAfterSend() {
        if (messageEditText != null) {
            messageEditText.setText("");
        }
        selectedImageUri = null;
        selectedVideoUri = null;
        selectedAudioUri = null;
        selectedAudioName = null;
        releaseComposerAudioPlayer();
        renderComposerAttachments();
    }

    @Nullable
    private Uri resolveSelectedAttachmentUri() {
        if (selectedImageUri != null) {
            return selectedImageUri;
        }
        if (selectedVideoUri != null) {
            return selectedVideoUri;
        }
        return selectedAudioUri;
    }

    @Nullable
    private String resolveSelectedAttachmentType() {
        if (selectedImageUri != null) {
            return ChatRepository.ATTACHMENT_TYPE_IMAGE;
        }
        if (selectedVideoUri != null) {
            return ChatRepository.ATTACHMENT_TYPE_VIDEO;
        }
        if (selectedAudioUri != null) {
            return ChatRepository.ATTACHMENT_TYPE_AUDIO;
        }
        return null;
    }

    private void setSendingState(boolean sending) {
        sendingMessage = sending;
        if (sendButton != null) {
            sendButton.setEnabled(!sending);
            sendButton.setAlpha(sending ? 0.55f : 1f);
        }
        if (pickImageButton != null) {
            pickImageButton.setEnabled(!sending);
        }
        if (pickVideoButton != null) {
            pickVideoButton.setEnabled(!sending);
        }
        if (recordAudioButton != null) {
            recordAudioButton.setEnabled(!sending);
        }
        if (messageEditText != null) {
            messageEditText.setEnabled(!sending);
        }

        if (sending) {
            updateSubtitleText(activity.getString(R.string.chat_sending_message));
        } else if (!loadingMessages) {
            updateSubtitleText(chatMessages.isEmpty()
                    ? activity.getString(R.string.chat_empty_state)
                    : activity.getString(R.string.chat_subtitle));
        }
    }

    private void upsertMessage(@NonNull ChatMessage message) {
        int existingIndex = findMessageIndexById(message.getId());
        if (existingIndex >= 0) {
            chatMessages.set(existingIndex, message);
        } else {
            chatMessages.add(message);
        }
        chatsAdapter.setMessages(chatMessages);
        scrollToBottom();
    }

    private int findMessageIndexById(long messageId) {
        if (messageId <= 0L) {
            return -1;
        }
        for (int i = 0; i < chatMessages.size(); i++) {
            ChatMessage current = chatMessages.get(i);
            if (current != null && current.getId() == messageId) {
                return i;
            }
        }
        return -1;
    }

    private void scrollToBottom() {
        if (chatsAdapter == null || chatRecyclerView == null) {
            return;
        }
        int count = chatsAdapter.getItemCount();
        if (count <= 0) {
            return;
        }
        chatRecyclerView.scrollToPosition(count - 1);
    }

    private void openMediaAttachment(@NonNull ChatMessage message) {
        Uri uri = message.getAttachmentUri();
        if (uri == null) {
            return;
        }

        String mimeType = message.getType() == ChatMessage.Type.IMAGE ? "image/*" : "video/*";
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, mimeType)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            activity.startActivity(intent);
        } catch (Exception ignored) {
            Toast.makeText(activity, R.string.chat_media_open_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleMessageAudioPlayback(@NonNull ChatMessage message) {
        Uri audioUri = message.getAttachmentUri();
        if (audioUri == null) {
            return;
        }

        if (playingAudioMessageId != null
                && playingAudioMessageId == message.getId()
                && messageAudioPlayer != null
                && messageAudioReady) {
            if (messageAudioPlaying) {
                pauseMessageAudioPlayback();
            } else {
                try {
                    messageAudioPlayer.start();
                    messageAudioPlaying = true;
                    chatsAdapter.notifyDataSetChanged();
                } catch (IllegalStateException ignored) {
                    releaseMessageAudioPlayer();
                    chatsAdapter.notifyDataSetChanged();
                }
            }
            return;
        }

        startMessageAudioPlayback(message, audioUri);
    }

    private void startMessageAudioPlayback(@NonNull ChatMessage message, @NonNull Uri audioUri) {
        releaseMessageAudioPlayer();
        pauseComposerAudioPlayback();

        MediaPlayer player = new MediaPlayer();
        messageAudioPlayer = player;
        playingAudioMessageId = message.getId();
        messageAudioReady = false;
        messageAudioPlaying = false;
        chatsAdapter.notifyDataSetChanged();

        player.setOnPreparedListener(mp -> {
            messageAudioReady = true;
            try {
                mp.start();
                messageAudioPlaying = true;
            } catch (IllegalStateException ignored) {
                releaseMessageAudioPlayer();
                chatsAdapter.notifyDataSetChanged();
                return;
            }
            chatsAdapter.notifyDataSetChanged();
        });
        player.setOnCompletionListener(mp -> {
            messageAudioPlaying = false;
            if (messageAudioReady) {
                try {
                    mp.seekTo(0);
                } catch (IllegalStateException ignored) {
                    // Ignore.
                }
            }
            chatsAdapter.notifyDataSetChanged();
        });
        player.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(activity, R.string.report_attachment_audio_play_failed, Toast.LENGTH_SHORT).show();
            releaseMessageAudioPlayer();
            chatsAdapter.notifyDataSetChanged();
            return true;
        });

        try {
            player.setDataSource(activity, audioUri);
            player.prepareAsync();
        } catch (IOException | SecurityException | IllegalStateException e) {
            Toast.makeText(activity, R.string.report_attachment_audio_play_failed, Toast.LENGTH_SHORT).show();
            releaseMessageAudioPlayer();
            chatsAdapter.notifyDataSetChanged();
        }
    }

    private void pauseMessageAudioPlayback() {
        if (messageAudioPlayer == null || !messageAudioReady) {
            return;
        }
        try {
            messageAudioPlayer.pause();
            messageAudioPlaying = false;
            chatsAdapter.notifyDataSetChanged();
        } catch (IllegalStateException ignored) {
            releaseMessageAudioPlayer();
            chatsAdapter.notifyDataSetChanged();
        }
    }

    private void releaseMessageAudioPlayer() {
        if (messageAudioPlayer != null) {
            try {
                if (messageAudioReady) {
                    messageAudioPlayer.stop();
                }
            } catch (IllegalStateException ignored) {
                // Ignore.
            }
            try {
                messageAudioPlayer.reset();
            } catch (IllegalStateException ignored) {
                // Ignore.
            }
            messageAudioPlayer.release();
            messageAudioPlayer = null;
        }
        messageAudioReady = false;
        messageAudioPlaying = false;
        playingAudioMessageId = null;
    }

    private void releaseAudioRecorder() {
        if (audioRecorder != null) {
            try {
                audioRecorder.reset();
            } catch (IllegalStateException ignored) {
                // Ignore.
            }
            audioRecorder.release();
            audioRecorder = null;
        }
    }

    private long nextMessageId() {
        long id = localMessageId;
        localMessageId++;
        return id;
    }

    private void persistReadPermission(@NonNull Uri uri) {
        try {
            activity.getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (SecurityException | IllegalArgumentException ignored) {
            // Some providers do not support persist permission.
        }
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
                    String displayName = cursor.getString(index);
                    if (!isBlank(displayName)) {
                        return displayName.trim();
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

        String lastPathSegment = uri.getLastPathSegment();
        if (!isBlank(lastPathSegment)) {
            return lastPathSegment.trim();
        }
        return fallback;
    }

    private void updateSubtitleText(@NonNull String text) {
        if (chatSubtitleText != null) {
            chatSubtitleText.setText(text);
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

    private boolean isBlank(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }
}
