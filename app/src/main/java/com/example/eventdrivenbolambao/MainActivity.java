package com.example.eventdrivenbolambao;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    EditText emailInput;
    EditText passwordInput;
    Button loginButton;
    TextView signupText;

    private static final String API_URL =
            "https://user-api-dtfg.onrender.com/users";

    public static final String EXTRA_TEST_API_URL =
            "test_api_url";

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signupText = findViewById(R.id.signupText);

        requestQueue = Volley.newRequestQueue(this);

        loginButton.setOnClickListener(v -> {

            String email = emailInput.getText()
                    .toString().trim();

            String password = passwordInput.getText()
                    .toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                        "Please enter your email and password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            loginUsingAPI(email, password);
        });

        signupText.setOnClickListener(v -> {
            startActivity(new Intent(
                    this, SignupActivity.class));
        });
    }

    private void loginUsingAPI(String email, String password) {

        loginButton.setEnabled(false);

        Toast.makeText(this,
                "Connecting to server...",
                Toast.LENGTH_SHORT).show();

        String url = API_URL;

        // Optional local server for instrumented tests.
        if (BuildConfig.DEBUG) {

            String testUrl = getIntent()
                    .getStringExtra(EXTRA_TEST_API_URL);

            if (testUrl != null &&
                    (testUrl.startsWith("http://localhost:")
                            || testUrl.startsWith("http://127.0.0.1:"))) {
                url = testUrl;
            }
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,

                response -> {

                    loginButton.setEnabled(true);

                    boolean matched = false;

                    for (int i = 0; i < response.length(); i++) {

                        JSONObject user =
                                response.optJSONObject(i);

                        if (user == null) continue;

                        String savedEmail =
                                user.optString("Email", "");

                        String savedPassword =
                                user.optString("Password", "");

                        if (email.equalsIgnoreCase(savedEmail)
                                && password.equals(savedPassword)) {

                            matched = true;
                            break;
                        }
                    }

                    if (matched) {

                        String code = generateVerificationCode();

                        Toast.makeText(this,
                                "Verification code: " + code,
                                Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(
                                this, VerificationActivity.class);

                        intent.putExtra("verificationCode", code);
                        intent.putExtra("email", email);

                        startActivity(intent);

                    } else {

                        Toast.makeText(this,
                                "Invalid email or password",
                                Toast.LENGTH_LONG).show();
                    }
                },

                error -> {

                    loginButton.setEnabled(true);

                    Log.e("RENDER_LOGIN",
                            "Login request failed", error);

                    String message =
                            "Unable to connect to server";

                    if (error.networkResponse != null) {
                        message += " (HTTP " +
                                error.networkResponse.statusCode + ")";
                    }

                    Toast.makeText(this,
                            message, Toast.LENGTH_LONG).show();
                }
        );

        request.setRetryPolicy(
                new DefaultRetryPolicy(60000, 0, 1.0f)
        );

        requestQueue.add(request);
    }

    private String generateVerificationCode() {

        Random random = new Random();

        int code = 100000 + random.nextInt(900000);

        return String.valueOf(code);
    }

    @Override
    protected void onDestroy() {

        if (requestQueue != null) {
            requestQueue.cancelAll(request -> true);
        }

        super.onDestroy();
    }
}
