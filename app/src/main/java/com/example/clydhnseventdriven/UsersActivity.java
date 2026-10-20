
package com.example.clydhnseventdriven;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class UsersActivity extends AppCompatActivity {

    private static final String API_URL =
            "https://user-api-dtfg.onrender.com/users";

    private LinearLayout usersContainer;
    private TextView statusText;
    private Button refreshButton;
    private Button backButton;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        usersContainer = findViewById(R.id.usersContainer);
        statusText = findViewById(R.id.statusText);
        refreshButton = findViewById(R.id.refreshButton);
        backButton = findViewById(R.id.backButton);

        requestQueue = Volley.newRequestQueue(this);

        refreshButton.setOnClickListener(v -> loadUsers());
        backButton.setOnClickListener(v -> finish());

        loadUsers();
    }

    private void loadUsers() {

        usersContainer.removeAllViews();

        statusText.setText(
                "Loading users from Render..."
        );

        refreshButton.setEnabled(false);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                API_URL,
                null,

                response -> {

                    refreshButton.setEnabled(true);

                    int count = 0;

                    for (int i = 0; i < response.length(); i++) {

                        JSONObject user =
                                response.optJSONObject(i);

                        if (user == null) continue;

                        String firstName =
                                user.optString("FirstName", "");

                        String lastName =
                                user.optString("LastName", "");

                        String email =
                                user.optString("Email", "");

                        String fullName =
                                (firstName + " " + lastName).trim();

                        if (fullName.isEmpty()) {
                            fullName = "Unnamed User";
                        }

                        addUserCard(fullName, email);
                        count++;
                    }

                    statusText.setText(
                            count + " registered user(s)"
                    );
                },

                error -> {

                    refreshButton.setEnabled(true);

                    statusText.setText(
                            "Unable to load registered users"
                    );

                    Toast.makeText(this,
                            "Could not connect to Render",
                            Toast.LENGTH_LONG).show();
                }
        );

        request.setRetryPolicy(
                new DefaultRetryPolicy(60000, 0, 1.0f)
        );

        requestQueue.add(request);
    }

    private void addUserCard(String name, String email) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);

        card.setPadding(
                dp(18), dp(15), dp(18), dp(15)
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(14));

        background.setStroke(
                dp(1),
                Color.rgb(224, 228, 234)
        );

        card.setBackground(background);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, dp(12));

        TextView nameView = new TextView(this);

        nameView.setText(name);
        nameView.setTextSize(18);
        nameView.setTypeface(null, Typeface.BOLD);
        nameView.setTextColor(Color.rgb(23, 23, 23));

        TextView emailView = new TextView(this);

        emailView.setText(email);
        emailView.setTextSize(14);
        emailView.setTextColor(Color.rgb(90, 99, 115));
        emailView.setPadding(0, dp(6), 0, 0);

        card.addView(nameView);
        card.addView(emailView);

        usersContainer.addView(card, params);
    }

    private int dp(int value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics().density
        );
    }

    @Override
    protected void onDestroy() {

        if (requestQueue != null) {
            requestQueue.cancelAll(request -> true);
        }

        super.onDestroy();
    }
}
