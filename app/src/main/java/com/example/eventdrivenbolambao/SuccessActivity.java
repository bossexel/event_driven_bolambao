package com.example.eventdrivenbolambao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SuccessActivity extends AppCompatActivity {

    private Button viewUsersButton;
    private Button logoutButton;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_success);

        viewUsersButton = findViewById(R.id.viewUsersButton);
        logoutButton = findViewById(R.id.logoutButton);
        welcomeText = findViewById(R.id.welcomeText);

        String email = getIntent().getStringExtra("email");

        if (email != null && !email.isEmpty()) {
            welcomeText.setText(
                    "Successfully verified!\n" + email
            );
        } else {
            welcomeText.setText(
                    "Successfully verified!"
            );
        }

        viewUsersButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SuccessActivity.this,
                    UsersActivity.class
            );

            intent.putExtra("email", email);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SuccessActivity.this,
                    MainActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
    }
}
