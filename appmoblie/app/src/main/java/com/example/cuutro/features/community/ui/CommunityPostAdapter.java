package com.example.cuutro.features.community.ui;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuutro.R;
import com.example.cuutro.features.community.model.CommunityPostItem;
import com.example.cuutro.features.report.ui.controller.ReportBitmapLoader;

import java.util.ArrayList;
import java.util.List;

public class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.CommunityPostViewHolder> {

    public interface Listener {
        void onActionClicked(@NonNull CommunityPostItem item, int position);
    }

    private final List<CommunityPostItem> items = new ArrayList<>();
    @Nullable
    private final Listener listener;
    private final ReportBitmapLoader bitmapLoader = new ReportBitmapLoader();

    public CommunityPostAdapter(@Nullable Listener listener) {
        this.listener = listener;
    }

    public void submitList(@NonNull List<CommunityPostItem> posts) {
        items.clear();
        items.addAll(posts);
        notifyDataSetChanged();
    }

    public void release() {
        bitmapLoader.shutdown();
    }

    @NonNull
    @Override
    public CommunityPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_community_post, parent, false);
        return new CommunityPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityPostViewHolder holder, int position) {
        CommunityPostItem item = items.get(position);
        holder.bind(item, bitmapLoader);
        holder.actionButton.setOnClickListener(v -> {
            if (listener == null) {
                return;
            }
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }
            listener.onActionClicked(items.get(currentPosition), currentPosition);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CommunityPostViewHolder extends RecyclerView.ViewHolder {

        private final ImageView avatarView;
        private final TextView authorNameView;
        private final ImageView verifiedView;
        private final TextView locationView;
        private final ImageView postImageView;
        private final TextView mediaCounterView;
        private final TextView likedByView;
        private final TextView captionView;
        private final TextView postDateView;
        private final ImageButton actionButton;

        CommunityPostViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.ivCommunityPostAvatar);
            authorNameView = itemView.findViewById(R.id.tvCommunityPostAuthor);
            verifiedView = itemView.findViewById(R.id.ivCommunityPostVerified);
            locationView = itemView.findViewById(R.id.tvCommunityPostLocation);
            postImageView = itemView.findViewById(R.id.ivCommunityPostImage);
            mediaCounterView = itemView.findViewById(R.id.tvCommunityPostMediaCounter);
            likedByView = itemView.findViewById(R.id.tvCommunityPostLikedBy);
            captionView = itemView.findViewById(R.id.tvCommunityPostCaption);
            postDateView = itemView.findViewById(R.id.tvCommunityPostDate);
            actionButton = itemView.findViewById(R.id.btnCommunityPostAction);
        }

        void bind(@NonNull CommunityPostItem item, @NonNull ReportBitmapLoader bitmapLoader) {
            bindAvatar(item, bitmapLoader);
            authorNameView.setText(item.getAuthorName());
            verifiedView.setVisibility(item.isVerified() ? View.VISIBLE : View.GONE);
            locationView.setText(item.getLocation());
            bindPostImage(item, bitmapLoader);
            String mediaCounter = item.getMediaCounter();
            mediaCounterView.setText(mediaCounter);
            mediaCounterView.setVisibility(mediaCounter == null || mediaCounter.trim().isEmpty()
                    ? View.GONE
                    : View.VISIBLE);
            String likedBy = item.getLikedByText();
            likedByView.setText(likedBy);
            likedByView.setVisibility(likedBy == null || likedBy.trim().isEmpty()
                    ? View.GONE
                    : View.VISIBLE);
            captionView.setText(buildCaption(item.getCaptionText()));
            postDateView.setText(item.getPostDate());
        }

        private void bindAvatar(
                @NonNull CommunityPostItem item,
                @NonNull ReportBitmapLoader bitmapLoader
        ) {
            avatarView.setTag(null);
            avatarView.setImageResource(item.getAuthorAvatarResId());
            avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            String avatarUrl = bitmapLoader.normalizeUrl(item.getAuthorAvatarUrl());
            if (avatarUrl == null || !bitmapLoader.isHttpUrl(avatarUrl)) {
                return;
            }

            avatarView.setTag(avatarUrl);
            bitmapLoader.load(avatarUrl, (loadedUrl, bitmap) -> {
                Object boundTag = avatarView.getTag();
                if (!(boundTag instanceof String)) {
                    return;
                }
                String expectedUrl = (String) boundTag;
                if (loadedUrl == null || !expectedUrl.equals(loadedUrl) || bitmap == null) {
                    return;
                }
                avatarView.setImageBitmap(bitmap);
                avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            });
        }

        private void bindPostImage(
                @NonNull CommunityPostItem item,
                @NonNull ReportBitmapLoader bitmapLoader
        ) {
            postImageView.setTag(null);
            postImageView.setImageResource(item.getImageResId());
            postImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            String imageUrl = bitmapLoader.normalizeUrl(item.getImageUrl());
            if (imageUrl == null || !bitmapLoader.isHttpUrl(imageUrl)) {
                return;
            }

            postImageView.setTag(imageUrl);
            bitmapLoader.load(imageUrl, (loadedUrl, bitmap) -> {
                Object boundTag = postImageView.getTag();
                if (!(boundTag instanceof String)) {
                    return;
                }
                String expectedUrl = (String) boundTag;
                if (loadedUrl == null || !expectedUrl.equals(loadedUrl) || bitmap == null) {
                    return;
                }
                postImageView.setImageBitmap(bitmap);
                postImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            });
        }

        @NonNull
        private CharSequence buildCaption(@NonNull String captionText) {
            int spaceIndex = captionText.indexOf(' ');
            if (spaceIndex <= 0) {
                return captionText;
            }
            SpannableString spannable = new SpannableString(captionText);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, spaceIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannable;
        }
    }
}
