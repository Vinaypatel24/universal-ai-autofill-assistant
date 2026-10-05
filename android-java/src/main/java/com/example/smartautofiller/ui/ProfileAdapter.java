package com.example.smartautofiller.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartautofiller.R;
import com.example.smartautofiller.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter displaying saved UserProfiles.
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    public interface OnProfileClickListener {
        void onProfileClick(UserProfile profile);
        void onDeleteClick(UserProfile profile);
    }

    private List<UserProfile> profiles = new ArrayList<>();
    private final OnProfileClickListener listener;

    public ProfileAdapter(OnProfileClickListener listener) {
        this.listener = listener;
    }

    public void setProfiles(List<UserProfile> newProfiles) {
        this.profiles = (newProfiles != null) ? newProfiles : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_card, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        UserProfile profile = profiles.get(position);
        holder.bind(profile, listener);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAvatar;
        private final TextView tvProfileName;
        private final TextView tvFullName;
        private final TextView tvEmail;
        private final TextView tvPhone;
        private final TextView tvSectionsCount;
        private final ImageView btnDelete;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.card_avatar);
            tvProfileName = itemView.findViewById(R.id.card_profile_name);
            tvFullName = itemView.findViewById(R.id.card_full_name);
            tvEmail = itemView.findViewById(R.id.card_email);
            tvPhone = itemView.findViewById(R.id.card_phone);
            tvSectionsCount = itemView.findViewById(R.id.card_sections_count);
            btnDelete = itemView.findViewById(R.id.btn_delete_profile);
        }

        public void bind(final UserProfile profile, final OnProfileClickListener listener) {
            String pName = profile.getProfileName();
            tvProfileName.setText(pName.isEmpty() ? "Profile" : pName);
            tvFullName.setText(profile.getFullName().isEmpty() ? "No Name" : profile.getFullName());
            tvEmail.setText("✉️ " + (profile.getEmail().isEmpty() ? "No Email" : profile.getEmail()));
            tvPhone.setText("📞 " + (profile.getPhoneNumber().isEmpty() ? "No Phone" : profile.getPhoneNumber()));

            if (!pName.isEmpty()) {
                tvAvatar.setText(pName.substring(0, 1).toUpperCase());
            }

            int sectionCount = profile.getSections() != null ? profile.getSections().size() : 0;
            tvSectionsCount.setText("📋 " + sectionCount + " Structured Section" + (sectionCount == 1 ? "" : "s"));

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(profile);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onProfileClick(profile);
            });
        }
    }
}
