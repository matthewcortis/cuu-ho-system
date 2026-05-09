package com.example.cuutro.features.captain.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuutro.R;
import com.example.cuutro.features.captain.data.model.CaptainTeamMemberItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CaptainTeamMemberAdapter extends RecyclerView.Adapter<CaptainTeamMemberAdapter.CaptainTeamMemberViewHolder> {

    private final List<CaptainTeamMemberItem> items = new ArrayList<>();

    public void submitList(@NonNull List<CaptainTeamMemberItem> members) {
        items.clear();
        items.addAll(members);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CaptainTeamMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_captain_team_member, parent, false);
        return new CaptainTeamMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CaptainTeamMemberViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CaptainTeamMemberViewHolder extends RecyclerView.ViewHolder {

        private final TextView initialTextView;
        private final TextView nameTextView;
        private final TextView phoneTextView;
        private final TextView roleTextView;

        CaptainTeamMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            initialTextView = itemView.findViewById(R.id.tvCaptainMemberInitial);
            nameTextView = itemView.findViewById(R.id.tvCaptainMemberName);
            phoneTextView = itemView.findViewById(R.id.tvCaptainMemberPhone);
            roleTextView = itemView.findViewById(R.id.tvCaptainMemberRole);
        }

        void bind(@NonNull CaptainTeamMemberItem item) {
            nameTextView.setText(item.getName());
            String phone = item.getPhone();
            if (phone.trim().isEmpty()) {
                phoneTextView.setText(R.string.captain_member_phone_empty);
            } else {
                phoneTextView.setText(phone);
            }
            roleTextView.setText(item.isLeader()
                    ? R.string.captain_member_role_leader
                    : R.string.captain_member_role_member);
            initialTextView.setText(resolveInitial(item.getName()));
        }

        @NonNull
        private String resolveInitial(@NonNull String rawName) {
            String trimmed = rawName.trim();
            if (trimmed.isEmpty()) {
                return "?";
            }
            String firstChar = String.valueOf(trimmed.charAt(0));
            return firstChar.toUpperCase(Locale.ROOT);
        }
    }
}
