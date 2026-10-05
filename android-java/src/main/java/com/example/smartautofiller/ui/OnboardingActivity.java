package com.example.smartautofiller.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.smartautofiller.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Onboarding Feature Tour activity providing an introduction to the
 * universal autofill floating bubble, document OCR scanner, and offline security.
 */
public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView tvIndicator;
    private Button btnNext;
    private Button btnSkip;

    private final List<OnboardingItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("autofill_prefs", MODE_PRIVATE);
        if (prefs.getBoolean("onboarding_done", false)) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.view_pager_onboarding);
        tvIndicator = findViewById(R.id.tv_page_indicator);
        btnNext = findViewById(R.id.btn_next);
        btnSkip = findViewById(R.id.btn_skip);

        setupData();
        setupViewPager();

        btnSkip.setOnClickListener(v -> finishOnboarding());
        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < items.size() - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                finishOnboarding();
            }
        });
    }

    private void setupData() {
        items.add(new OnboardingItem(
                "🪄",
                "Universal Form Fill",
                "Populate forms instantly across any Android app, browser, or hybrid WebView via a single tap on the floating overlay bubble."
        ));
        items.add(new OnboardingItem(
                "📸",
                "Smart Document Scanner",
                "Point your camera at PAN cards, Aadhaar cards, and marksheets. On-device OCR automatically extracts details into structured profile sections."
        ));
        items.add(new OnboardingItem(
                "🌐",
                "Regional Language Support",
                "Sub-millisecond offline translation for Hindi, Tamil, Telugu, and more so English profiles effortlessly fill regional language forms."
        ));
        items.add(new OnboardingItem(
                "🔒",
                "100% Offline & Private",
                "Hardware-backed AES-256 encrypted sandbox with PIN lockout. No cloud uploads. Your personal data stays strictly on your device."
        ));
    }

    private void setupViewPager() {
        viewPager.setAdapter(new OnboardingAdapter(items));
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicator(position);
            }
        });
    }

    private void updateIndicator(int position) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i == position) {
                sb.append("● ");
            } else {
                sb.append("○ ");
            }
        }
        tvIndicator.setText(sb.toString().trim());

        if (position == items.size() - 1) {
            btnNext.setText("Get Started");
        } else {
            btnNext.setText("Next");
        }
    }

    private void finishOnboarding() {
        SharedPreferences prefs = getSharedPreferences("autofill_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("onboarding_done", true).apply();
        navigateToMain();
    }

    private void navigateToMain() {
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    static class OnboardingItem {
        final String icon;
        final String title;
        final String description;

        OnboardingItem(String icon, String title, String description) {
            this.icon = icon;
            this.title = title;
            this.description = description;
        }
    }

    static class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {
        private final List<OnboardingItem> items;

        OnboardingAdapter(List<OnboardingItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding_page, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OnboardingItem item = items.get(position);
            holder.tvIcon.setText(item.icon);
            holder.tvTitle.setText(item.title);
            holder.tvDesc.setText(item.description);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView tvIcon;
            final TextView tvTitle;
            final TextView tvDesc;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvIcon = itemView.findViewById(R.id.tv_page_icon);
                tvTitle = itemView.findViewById(R.id.tv_page_title);
                tvDesc = itemView.findViewById(R.id.tv_page_desc);
            }
        }
    }
}
