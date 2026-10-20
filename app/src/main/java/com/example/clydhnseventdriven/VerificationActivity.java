
package com.example.clydhnseventdriven;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class VerificationActivity extends AppCompatActivity {

    private EditText codeInput;
    private Button verifyButton;
    private TextView verificationCodeText;
    private TextView timerText;

    private String verificationCode;
    private String email;

    private CountDownTimer countDownTimer;
    private boolean codeExpired = false;
    private boolean verified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_verification);

        codeInput = findViewById(R.id.codeInput);
        verifyButton = findViewById(R.id.verifyButton);
        verificationCodeText =
                findViewById(R.id.verificationCodeText);
        timerText = findViewById(R.id.timerText);

        verificationCode =
                getIntent().getStringExtra("verificationCode");

        email = getIntent().getStringExtra("email");

        if (verificationCode == null ||
                !verificationCode.matches("\\d{6}")) {

            Toast.makeText(
                    this,
                    "Invalid verification session. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        // Displays code for classroom demonstration only.
        verificationCodeText.setText(verificationCode);

        startTimer();

        verifyButton.setOnClickListener(v -> verifyCode());
    }

    private void verifyCode() {

        if (verified) {
            return;
        }

        if (codeExpired) {

            Toast.makeText(
                    this,
                    "Verification code has expired",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String enteredCode =
                codeInput.getText().toString().trim();

        if (enteredCode.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter the verification code",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!enteredCode.matches("\\d{6}")) {

            Toast.makeText(
                    this,
                    "Please enter a valid 6-digit code",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Only the exact correct OTP can proceed.
        if (!enteredCode.equals(verificationCode)) {

            codeInput.setError("Incorrect verification code");

            Toast.makeText(
                    this,
                    "Incorrect verification code",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        verified = true;
        verifyButton.setEnabled(false);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        Toast.makeText(
                this,
                "Verification successful!",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(
                VerificationActivity.this,
                SuccessActivity.class
        );

        intent.putExtra("email", email);

        startActivity(intent);
        finish();
    }

    private void startTimer() {

        countDownTimer = new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                long seconds = millisUntilFinished / 1000;

                timerText.setText(
                        "Code expires in " + seconds + " seconds"
                );
            }

            @Override
            public void onFinish() {

                codeExpired = true;

                timerText.setText(
                        "Verification code expired"
                );

                codeInput.setEnabled(false);
                verifyButton.setEnabled(false);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
