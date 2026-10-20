package com.example.eventdrivenbolambao;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity {

    EditText nameInput;
    EditText emailInput;
    EditText passwordInput;
    EditText confirmPasswordInput;

    Button signupButton;
    TextView loginText;

    private static final String API_URL = "https://users-api-exiel.onrender.com";

    private RequestQueue requestQueue;
    private boolean registering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);

        signupButton = findViewById(R.id.signupButton);
        loginText = findViewById(R.id.loginText);

        requestQueue = Volley.newRequestQueue(this);

        signupButton.setOnClickListener(v -> registerUser());

        loginText.setOnClickListener(v -> {
            if (!registering) {
                goToLogin();
            }
        });
    }

    private void registerUser() {

        if (registering)
            return;

        String name = nameInput.getText()
                .toString().trim();

        String email = emailInput.getText()
                .toString().trim();

        String password = passwordInput.getText()
                .toString();

        String confirm = confirmPasswordInput.getText()
                .toString();

        if (name.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirm.isEmpty()) {

            Toast.makeText(this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError(
                    "Password must have at least 6 characters");
            return;
        }

        if (!password.equals(confirm)) {
            confirmPasswordInput.setError(
                    "Passwords do not match");
            return;
        }

        String firstName;
        String lastName;

        int space = name.lastIndexOf(' ');

        if (space < 0) {
            firstName = name;
            lastName = "";
        } else {
            firstName = name.substring(0, space).trim();
            lastName = name.substring(space + 1).trim();
        }

        JSONObject newUser = new JSONObject();

        try {
            newUser.put("FirstName", firstName);
            newUser.put("LastName", lastName);
            newUser.put("Email", email);
            newUser.put("Password", password);

        } catch (JSONException e) {
            Toast.makeText(this,
                    "Unable to prepare registration",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Preliminary duplicate email check.
        JsonArrayRequest checkRequest = new JsonArrayRequest(
                Request.Method.GET,
                API_URL,
                null,

                response -> {

                    boolean exists = false;

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject user = response.optJSONObject(i);

                        if (user == null)
                            continue;

                        if (email.equalsIgnoreCase(
                                user.optString("Email", ""))) {
                            exists = true;
                            break;
                        }
                    }

                    if (exists) {
                        setLoading(false);
                        emailInput.setError(
                                "Email already registered");

                        Toast.makeText(this,
                                "This email already exists",
                                Toast.LENGTH_LONG).show();
                    } else {
                        saveToRender(newUser);
                    }
                },

                error -> {
                    setLoading(false);
                    Toast.makeText(this,
                            "Unable to check registered accounts",
                            Toast.LENGTH_LONG).show();
                });

        checkRequest.setRetryPolicy(
                new DefaultRetryPolicy(60000, 0, 1.0f));

        requestQueue.add(checkRequest);
    }

    private void saveToRender(JSONObject newUser) {

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                API_URL,
                newUser,

                response -> {
                    setLoading(false);

                    Toast.makeText(this,
                            "Account created successfully! Please login.",
                            Toast.LENGTH_LONG).show();

                    goToLogin();
                },

                error -> {
                    setLoading(false);

                    int status = error.networkResponse == null
                            ? -1
                            : error.networkResponse.statusCode;

                    String message;

                    if (status == 409) {
                        message = "Email already registered";
                    } else if (status == 404 || status == 405) {
                        message = "Registration API is unavailable";
                    } else if (status >= 500) {
                        message = "Render server error";
                    } else {
                        message = "Registration not confirmed. Check the server.";
                    }

                    Toast.makeText(this,
                            message, Toast.LENGTH_LONG).show();
                });

        // No automatic retry for account creation.
        request.setRetryPolicy(
                new DefaultRetryPolicy(60000, 0, 1.0f));

        requestQueue.add(request);
    }

    private void setLoading(boolean loading) {
        registering = loading;
        signupButton.setEnabled(!loading);
        loginText.setEnabled(!loading);
        signupButton.setText(
                loading ? "Creating Account..." : "Sign Up");
    }

    private void goToLogin() {
        Intent intent = new Intent(
                this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (requestQueue != null) {
            requestQueue.cancelAll(request -> true);
        }
        super.onDestroy();
    }
}
