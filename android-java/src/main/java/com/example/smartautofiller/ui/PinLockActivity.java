package com.example.smartautofiller.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartautofiller.R;
import com.example.smartautofiller.security.PinManager;

/**
 * 4-Digit PIN Lock screen to protect sensitive personal and ID information.
 * Uses PinManager for encrypted storage, attempt tracking, and lockout protection.
 */
public class PinLockActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "mode";
    public static final String MODE_SETUP = "setup";
    public static final String MODE_UNLOCK = "unlock";

    private final StringBuilder enteredPin = new StringBuilder();
    private String firstEnteredPin = null; // for confirm in setup mode

    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvError;
    private View dot1, dot2, dot3, dot4;
    private Button btnSkip;

    private boolean isSetupMode = false;
    private PinManager pinManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);

        pinManager = new PinManager(this);
        String mode = getIntent().getStringExtra(EXTRA_MODE);
        if (MODE_SETUP.equals(mode) || !pinManager.isPinSet()) {
            isSetupMode = true;
        }

        bindViews();
        setupKeypad();
        updateUIForMode();
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tv_pin_title);
        tvSubtitle = findViewById(R.id.tv_pin_subtitle);
        tvError = findViewById(R.id.tv_pin_error);

        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);

        btnSkip = findViewById(R.id.btn_skip_pin);
        btnSkip.setOnClickListener(v -> {
            if (isSetupMode) {
                // User skipped setting a PIN
                proceedToMain();
            } else {
                Toast.makeText(this, "PIN is required to open the app", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIForMode() {
        if (isSetupMode) {
            tvTitle.setText(firstEnteredPin == null ? "Create PIN" : "Confirm PIN");
            tvSubtitle.setText(firstEnteredPin == null ? "Choose a 4-digit PIN to secure your data" : "Re-enter your 4-digit PIN");
            btnSkip.setVisibility(View.VISIBLE);
            btnSkip.setText("Skip PIN setup");
        } else {
            tvTitle.setText("Enter PIN");
            tvSubtitle.setText("Enter your 4-digit PIN to continue");
            btnSkip.setVisibility(View.GONE);
        }
    }

    private void setupKeypad() {
        int[] numIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        for (int i = 0; i <= 9; i++) {
            final int digit = i;
            Button b = findViewById(numIds[i]);
            if (b != null) {
                b.setOnClickListener(v -> onDigitPressed(digit));
            }
        }

        Button btnClear = findViewById(R.id.btn_clear);
        if (btnClear != null) {
            btnClear.setOnClickListener(v -> clearPin());
        }

        Button btnDel = findViewById(R.id.btn_del);
        if (btnDel != null) {
            btnDel.setOnClickListener(v -> deleteLastDigit());
        }
    }

    private void onDigitPressed(int digit) {
        if (enteredPin.length() >= 4) return;
        enteredPin.append(digit);
        updateDots();
        tvError.setText("");

        if (enteredPin.length() == 4) {
            handleCompletePin(enteredPin.toString());
        }
    }

    private void deleteLastDigit() {
        if (enteredPin.length() > 0) {
            enteredPin.deleteCharAt(enteredPin.length() - 1);
            updateDots();
        }
    }

    private void clearPin() {
        enteredPin.setLength(0);
        updateDots();
        tvError.setText("");
    }

    private void updateDots() {
        int len = enteredPin.length();
        dot1.setBackgroundResource(len >= 1 ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty);
        dot2.setBackgroundResource(len >= 2 ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty);
        dot3.setBackgroundResource(len >= 3 ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty);
        dot4.setBackgroundResource(len >= 4 ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty);
    }

    private void handleCompletePin(String pin) {
        if (isSetupMode) {
            if (firstEnteredPin == null) {
                firstEnteredPin = pin;
                clearPin();
                updateUIForMode();
            } else {
                if (firstEnteredPin.equals(pin)) {
                    pinManager.setPin(pin);
                    Toast.makeText(this, "✅ PIN set successfully!", Toast.LENGTH_SHORT).show();
                    proceedToMain();
                } else {
                    firstEnteredPin = null;
                    clearPin();
                    tvError.setText("PINs did not match. Please try again.");
                    updateUIForMode();
                }
            }
        } else {
            // Unlock verification
            PinManager.VerifyResult result = pinManager.verifyPin(pin);
            if (result.isSuccess()) {
                setResult(RESULT_OK);
                proceedToMain();
            } else if (result.getStatus() == PinManager.VerifyResult.Status.LOCKED_OUT) {
                clearPin();
                tvError.setText("Too many attempts. Locked out for " + result.getValue() + "s");
            } else {
                clearPin();
                tvError.setText("Incorrect PIN. " + result.getValue() + " attempts remaining.");
            }
        }
    }

    private void proceedToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
