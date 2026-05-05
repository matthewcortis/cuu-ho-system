package com.example.cuutro.features.chat.ui;

import android.content.res.ColorStateList;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuutro.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatMessageViewHolder> {

    public interface MessageActionListener {
        void onAudioClicked(@NonNull ChatMessage message);

        void onMediaClicked(@NonNull ChatMessage message);

        boolean isMessageAudioPlaying(@NonNull ChatMessage message);
    }

    @NonNull
    private final List<ChatMessage> messages = new ArrayList<>();
    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final DateFormat timeFormat;
    @NonNull
    private final MessageActionListener actionListener;

    public ChatsAdapter(
            @NonNull LayoutInflater inflater,
            @NonNull DateFormat timeFormat,
            @NonNull MessageActionListener actionListener
    ) {
        this.inflater = inflater;
        this.timeFormat = timeFormat;
        this.actionListener = actionListener;
    }

    public void setMessages(@NonNull List<ChatMessage> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    @NonNull
    public ChatMessage getItem(int position) {
        return messages.get(position);
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_chat_message, parent, false);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        boolean outgoing = message.isOutgoing();

        holder.root.setGravity(outgoing ? Gravity.END : Gravity.START);

        holder.bubble.setBackgroundResource(
                outgoing ? R.drawable.bg_chat_message_outgoing : R.drawable.bg_chat_message_incoming
        );

        int primaryTextColor = ContextCompat.getColor(
                holder.itemView.getContext(),
                outgoing ? R.color.white : R.color.report_text_primary
        );
        int secondaryTextColor = ContextCompat.getColor(
                holder.itemView.getContext(),
                outgoing ? R.color.white : R.color.report_label_inactive
        );

        holder.senderText.setText(outgoing
                ? holder.itemView.getContext().getString(R.string.chat_sender_you)
                : message.getSenderName());
        holder.senderText.setTextColor(secondaryTextColor);

        String messageText = trimToNull(message.getText());
        if (messageText != null) {
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setText(messageText);
            holder.messageText.setTextColor(primaryTextColor);
        } else {
            holder.messageText.setVisibility(View.GONE);
            holder.messageText.setText("");
        }

        holder.timeText.setText(timeFormat.format(new Date(message.getTimestampMs())));
        holder.timeText.setTextColor(secondaryTextColor);

        bindMediaSection(holder, message, outgoing);
        bindAudioSection(holder, message, outgoing);
    }

    private void bindMediaSection(
            @NonNull ChatMessageViewHolder holder,
            @NonNull ChatMessage message,
            boolean outgoing
    ) {
        ChatMessage.Type type = message.getType();
        Uri attachmentUri = message.getAttachmentUri();
        if ((type != ChatMessage.Type.IMAGE && type != ChatMessage.Type.VIDEO) || attachmentUri == null) {
            holder.mediaFrame.setVisibility(View.GONE);
            holder.mediaPreview.setImageDrawable(null);
            holder.videoOverlay.setVisibility(View.GONE);
            holder.mediaPreview.setImageTintList(null);
            holder.mediaFrame.setOnClickListener(null);
            return;
        }

        holder.mediaFrame.setVisibility(View.VISIBLE);
        holder.mediaFrame.setOnClickListener(v -> actionListener.onMediaClicked(message));

        if (type == ChatMessage.Type.IMAGE) {
            holder.videoOverlay.setVisibility(View.GONE);
            if (isRemoteUri(attachmentUri)) {
                holder.mediaPreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                holder.mediaPreview.setImageResource(R.drawable.ic_report_camera);
                int tint = ContextCompat.getColor(
                        holder.itemView.getContext(),
                        outgoing ? R.color.white : R.color.report_action_red
                );
                holder.mediaPreview.setImageTintList(ColorStateList.valueOf(tint));
            } else {
                holder.mediaPreview.setImageTintList(null);
                holder.mediaPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.mediaPreview.setImageURI(attachmentUri);
            }
            return;
        }

        holder.videoOverlay.setVisibility(View.VISIBLE);
        holder.mediaPreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        holder.mediaPreview.setImageResource(R.drawable.ic_report_video);
        int tint = ContextCompat.getColor(
                holder.itemView.getContext(),
                outgoing ? R.color.white : R.color.report_action_red
        );
        holder.mediaPreview.setImageTintList(ColorStateList.valueOf(tint));
    }

    private void bindAudioSection(
            @NonNull ChatMessageViewHolder holder,
            @NonNull ChatMessage message,
            boolean outgoing
    ) {
        if (message.getType() != ChatMessage.Type.AUDIO || message.getAttachmentUri() == null) {
            holder.audioContainer.setVisibility(View.GONE);
            holder.audioNameText.setText("");
            holder.audioPlayButton.setOnClickListener(null);
            return;
        }

        holder.audioContainer.setVisibility(View.VISIBLE);
        String fallbackName = holder.itemView.getContext().getString(R.string.report_attachment_audio_default_name);
        String name = trimToNull(message.getAttachmentName());
        holder.audioNameText.setText(name != null ? name : fallbackName);
        holder.audioNameText.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), outgoing ? R.color.white : R.color.report_text_primary)
        );
        holder.audioPlayButton.setOnClickListener(v -> actionListener.onAudioClicked(message));

        boolean playing = actionListener.isMessageAudioPlaying(message);
        holder.audioPlayButton.setImageResource(
                playing ? R.drawable.ic_report_audio_pause : R.drawable.ic_report_audio_play
        );
        holder.audioPlayButton.setContentDescription(
                holder.itemView.getContext().getString(
                        playing ? R.string.report_attachment_audio_pause : R.string.report_attachment_audio_play
                )
        );
        int tint = ContextCompat.getColor(
                holder.itemView.getContext(),
                outgoing ? R.color.white : R.color.report_text_primary
        );
        holder.audioPlayButton.setImageTintList(ColorStateList.valueOf(tint));
    }

    @Nullable
    private String trimToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isRemoteUri(@NonNull Uri uri) {
        String scheme = uri.getScheme();
        if (scheme == null) {
            return false;
        }
        String lower = scheme.toLowerCase();
        return "http".equals(lower) || "https".equals(lower);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatMessageViewHolder extends RecyclerView.ViewHolder {

        final LinearLayout root;
        final LinearLayout bubble;
        final TextView senderText;
        final TextView messageText;
        final TextView timeText;
        final FrameLayout mediaFrame;
        final ImageView mediaPreview;
        final ImageView videoOverlay;
        final LinearLayout audioContainer;
        final ImageButton audioPlayButton;
        final TextView audioNameText;

        ChatMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.layoutChatMessageRoot);
            bubble = itemView.findViewById(R.id.layoutChatMessageBubble);
            senderText = itemView.findViewById(R.id.tvChatMessageSender);
            messageText = itemView.findViewById(R.id.tvChatMessageText);
            timeText = itemView.findViewById(R.id.tvChatMessageTime);
            mediaFrame = itemView.findViewById(R.id.layoutChatMessageMedia);
            mediaPreview = itemView.findViewById(R.id.ivChatMessageMediaPreview);
            videoOverlay = itemView.findViewById(R.id.ivChatMessageVideoOverlay);
            audioContainer = itemView.findViewById(R.id.layoutChatMessageAudio);
            audioPlayButton = itemView.findViewById(R.id.btnChatMessageAudioPlay);
            audioNameText = itemView.findViewById(R.id.tvChatMessageAudioName);
        }
    }
}
