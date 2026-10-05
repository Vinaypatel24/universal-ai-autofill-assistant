package com.example.smartautofiller.service;

import android.accessibilityservice.AccessibilityService;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartautofiller.database.AppDatabase;
import com.example.smartautofiller.matcher.FieldMatcher;
import com.example.smartautofiller.matcher.ShortcutExpander;
import com.example.smartautofiller.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Core Accessibility Service in Java.
 * Renders the floating draggable bubble overlay, parses active window UI trees,
 * matches field labels, and injects user profile data into target apps/browsers.
 */
public class SmartAccessibilityService extends AccessibilityService {

    private static SmartAccessibilityService instance;

    private WindowManager windowManager;
    private View floatingView;
    private View selectorView;
    private AppDatabase db;
    private boolean isFilling = false;
    private String currentPackageName = "";
    private AccessibilityNodeInfo lastFocusedNode = null;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static SmartAccessibilityService getInstance() {
        return instance;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        db = AppDatabase.getDatabase(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        if (event.getPackageName() != null && event.getPackageName().length() > 0) {
            currentPackageName = event.getPackageName().toString();
        }

        int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED ||
            eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source != null && (source.isEditable() || isEditText(source))) {
                lastFocusedNode = source;
            }
        }
    }

    @Override
    public void onInterrupt() {
        // Called when accessibility is interrupted
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeFloatingBubble();
        instance = null;
    }

    // ── Floating Bubble Overlay ────────────────────────────────
    public void setBubbleVisible(boolean visible) {
        if (visible) {
            if (floatingView == null) {
                showFloatingBubble();
            }
        } else {
            removeFloatingBubble();
        }
    }

    private void showFloatingBubble() {
        if (windowManager == null) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        // Inflate the floating bubble layout
        int layoutId = getResources().getIdentifier("layout_floating_bubble", "layout", getPackageName());
        if (layoutId == 0) return;
        floatingView = inflater.inflate(layoutId, null);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 200;

        final Handler longPressHandler = new Handler(Looper.getMainLooper());
        final boolean[] isLongPress = {false};
        final Runnable[] longPressRunnable = new Runnable[1];

        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean isMoveAction = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event == null) return false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isMoveAction = false;
                        isLongPress[0] = false;
                        longPressRunnable[0] = () -> {
                            isLongPress[0] = true;
                            showProfileSelector(params.x, params.y);
                        };
                        longPressHandler.postDelayed(longPressRunnable[0], 600);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - initialTouchX;
                        float dy = event.getRawY() - initialTouchY;
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isMoveAction = true;
                            if (longPressRunnable[0] != null) {
                                longPressHandler.removeCallbacks(longPressRunnable[0]);
                            }
                        }
                        params.x = initialX + (int) dx;
                        params.y = initialY + (int) dy;
                        if (floatingView != null && windowManager != null) {
                            windowManager.updateViewLayout(floatingView, params);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (longPressRunnable[0] != null) {
                            longPressHandler.removeCallbacks(longPressRunnable[0]);
                        }
                        Point size = new Point();
                        windowManager.getDefaultDisplay().getSize(size);
                        params.x = Math.max(0, Math.min(params.x, size.x - 150));
                        params.y = Math.max(0, Math.min(params.y, size.y - 150));
                        if (floatingView != null && windowManager != null) {
                            windowManager.updateViewLayout(floatingView, params);
                        }

                        if (!isMoveAction && !isLongPress[0]) {
                            if (v != null) v.performClick();
                            fillFormSmart(null);
                        }
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(floatingView, params);
    }

    private void showProfileSelector(int bubbleX, int bubbleY) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<UserProfile> profiles = db.userProfileDao().getAllProfilesList();
            mainHandler.post(() -> {
                if (profiles == null || profiles.isEmpty()) {
                    Toast.makeText(this, "No profiles found! Please add one.", Toast.LENGTH_SHORT).show();
                    return;
                }
                removeSelectorView();

                LayoutInflater inflater = LayoutInflater.from(this);
                int selLayoutId = getResources().getIdentifier("layout_profile_selector", "layout", getPackageName());
                if (selLayoutId == 0) return;
                View selectorLayout = inflater.inflate(selLayoutId, null);

                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );
                params.gravity = Gravity.TOP | Gravity.START;
                Point size = new Point();
                windowManager.getDefaultDisplay().getSize(size);
                params.x = Math.max(0, Math.min(bubbleX + 120, size.x - 300));
                params.y = Math.max(0, Math.min(bubbleY, size.y - 400));

                int containerId = getResources().getIdentifier("profile_container", "id", getPackageName());
                int closeBtnId = getResources().getIdentifier("btn_close_selector", "id", getPackageName());
                int itemLayoutId = getResources().getIdentifier("layout_profile_item", "layout", getPackageName());

                LinearLayout container = selectorLayout.findViewById(containerId);
                View closeBtn = selectorLayout.findViewById(closeBtnId);

                if (container != null && itemLayoutId != 0) {
                    for (UserProfile profile : profiles) {
                        View itemView = inflater.inflate(itemLayoutId, container, false);
                        int nameId = getResources().getIdentifier("profile_item_name", "id", getPackageName());
                        int subId = getResources().getIdentifier("profile_item_sub", "id", getPackageName());
                        int avatarId = getResources().getIdentifier("profile_item_avatar", "id", getPackageName());

                        TextView tvName = itemView.findViewById(nameId);
                        TextView tvSub = itemView.findViewById(subId);
                        TextView tvAvatar = itemView.findViewById(avatarId);

                        if (tvName != null) tvName.setText(profile.getProfileName());
                        if (tvSub != null) tvSub.setText(profile.getFullName());
                        if (tvAvatar != null && !profile.getProfileName().isEmpty()) {
                            tvAvatar.setText(profile.getProfileName().substring(0, 1).toUpperCase());
                        }

                        itemView.setOnClickListener(v -> {
                            removeSelectorView();
                            fillFormSmart(profile);
                        });
                        container.addView(itemView);
                    }
                }

                if (closeBtn != null) {
                    closeBtn.setOnClickListener(v -> removeSelectorView());
                }

                selectorView = selectorLayout;
                windowManager.addView(selectorLayout, params);
                mainHandler.postDelayed(this::removeSelectorView, 6000);
            });
        });
    }

    private void removeSelectorView() {
        if (selectorView != null && windowManager != null) {
            try {
                windowManager.removeView(selectorView);
            } catch (Exception ignored) {}
            selectorView = null;
        }
    }

    private void removeFloatingBubble() {
        removeSelectorView();
        if (floatingView != null && windowManager != null) {
            try {
                windowManager.removeView(floatingView);
            } catch (Exception ignored) {}
            floatingView = null;
        }
    }

    // ── Smart Form Fill Core ──────────────────────────────────
    private void fillFormSmart(UserProfile selectedProfile) {
        if (isFilling) return;
        isFilling = true;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<UserProfile> profiles = db.userProfileDao().getAllProfilesList();
                if (profiles == null || profiles.isEmpty()) {
                    mainHandler.post(() -> Toast.makeText(this, "No profiles found! Add one in the app.", Toast.LENGTH_SHORT).show());
                    return;
                }

                UserProfile profile = (selectedProfile != null) ? selectedProfile : profiles.get(0);

                mainHandler.post(() -> {
                    // Check if focused field has text shortcut expansion
                    if (lastFocusedNode != null && (lastFocusedNode.isEditable() || isEditText(lastFocusedNode))) {
                        CharSequence text = lastFocusedNode.getText();
                        String current = text != null ? text.toString() : "";
                        ShortcutExpander.ExpandResult res = ShortcutExpander.expandShortcuts(current, profile);
                        if (res.isFilled()) {
                            setTextToNode(lastFocusedNode, res.getUpdatedText());
                            Toast.makeText(this, "Expanded shortcut!", Toast.LENGTH_SHORT).show();
                            isFilling = false;
                            return;
                        }
                    }

                    // Otherwise, fill all fields across the visible window
                    fillAllFields(profile);
                    isFilling = false;
                });
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this, "Fill error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                isFilling = false;
            }
        });
    }

    private void fillAllFields(UserProfile profile) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        List<AccessibilityNodeInfo> editableNodes = new ArrayList<>();
        findEditableNodes(root, editableNodes);

        int filledCount = 0;
        for (AccessibilityNodeInfo node : editableNodes) {
            String label = extractNodeLabel(node);
            String matchedValue = FieldMatcher.matchFieldValue(label, profile);
            if (matchedValue != null && !matchedValue.isEmpty()) {
                boolean success = setTextToNode(node, matchedValue);
                if (success) filledCount++;
            }
        }

        final int count = filledCount;
        Toast.makeText(this, "Auto-filled " + count + " fields", Toast.LENGTH_SHORT).show();
    }

    private void findEditableNodes(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> list) {
        if (node == null) return;
        if (node.isEditable() || isEditText(node)) {
            list.add(node);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            findEditableNodes(node.getChild(i), list);
        }
    }

    private String extractNodeLabel(AccessibilityNodeInfo node) {
        if (node == null) return "";
        if (node.getHintText() != null && node.getHintText().length() > 0) {
            return node.getHintText().toString();
        }
        if (node.getContentDescription() != null && node.getContentDescription().length() > 0) {
            return node.getContentDescription().toString();
        }
        if (node.getViewIdResourceName() != null && node.getViewIdResourceName().length() > 0) {
            String id = node.getViewIdResourceName();
            return id.contains("/") ? id.substring(id.lastIndexOf("/") + 1) : id;
        }

        // Check parent siblings for nearest preceding label
        AccessibilityNodeInfo parent = node.getParent();
        if (parent != null) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                AccessibilityNodeInfo child = parent.getChild(i);
                if (child != null && child.equals(node) && i > 0) {
                    AccessibilityNodeInfo prev = parent.getChild(i - 1);
                    if (prev != null && prev.getText() != null) {
                        return prev.getText().toString();
                    }
                }
            }
        }
        return "";
    }

    private boolean setTextToNode(AccessibilityNodeInfo node, String text) {
        if (node == null || text == null) return false;
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }

    private boolean isEditText(AccessibilityNodeInfo node) {
        CharSequence className = node.getClassName();
        return className != null && className.toString().contains("EditText");
    }
}
