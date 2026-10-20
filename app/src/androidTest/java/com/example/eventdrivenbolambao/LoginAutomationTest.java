package com.example.eventdrivenbolambao;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class LoginAutomationTest {

    private ActivityScenario<MainActivity> scenario;

    private static final long DELAY = 1000;
    private static final long OTP_DELAY = 650;
    private static final long TIMEOUT = 90000;

    private String validEmail;
    private String validPassword;

    // Intentionally incorrect password
    private static final String WRONG_PASSWORD =
            "WrongPassword987!";

    @Before
    public void setUp() {

        Bundle arguments = InstrumentationRegistry.getArguments();
        validEmail = arguments.getString("testEmail", "");
        validPassword = arguments.getString("testPassword", "");
        assumeTrue("Supply testEmail and testPassword instrumentation arguments",
                !validEmail.trim().isEmpty() && !validPassword.isEmpty());

        Intent intent = new Intent(
                InstrumentationRegistry
                        .getInstrumentation()
                        .getTargetContext(),
                MainActivity.class
        );

        // Uses the configured API in MainActivity.
        scenario = ActivityScenario.launch(intent);

        pause(DELAY);
    }

    @Test
    public void testCompleteAutomation() {

        // =====================================
        // STEP 1: VALID LOGIN
        // =====================================

        onView(withId(R.id.emailInput))
                .perform(replaceText(validEmail));

        pause(DELAY);

        onView(withId(R.id.passwordInput))
                .perform(
                        replaceText(validPassword),
                        closeSoftKeyboard()
                );

        pause(DELAY);

        onView(withId(R.id.loginButton))
                .perform(click());

        waitForScreen(R.id.codeInput, TIMEOUT);

        // =====================================
        // STEP 2: READ CORRECT OTP
        // =====================================

        String correctOtp = readOtp();

        assertTrue(
                "OTP must contain six digits",
                correctOtp.matches("\\d{6}")
        );

        // =====================================
        // STEP 3: INVALID OTP FIRST
        // =====================================

        String wrongOtp =
                correctOtp.equals("000000")
                        ? "111111" : "000000";

        onView(withId(R.id.codeInput))
                .perform(
                        replaceText(wrongOtp),
                        closeSoftKeyboard()
                );

        pause(OTP_DELAY);

        onView(withId(R.id.verifyButton))
                .perform(click());

        pause(1000);

        onView(withId(R.id.codeInput))
                .check(matches(isDisplayed()));

        // =====================================
        // STEP 4: CORRECT OTP
        // =====================================

        onView(withId(R.id.codeInput))
                .perform(
                        replaceText(correctOtp),
                        closeSoftKeyboard()
                );

        pause(OTP_DELAY);

        onView(withId(R.id.verifyButton))
                .perform(click());

        waitForScreen(R.id.viewUsersButton, 15000);

        // =====================================
        // STEP 5: REGISTERED USERS
        // =====================================

        pause(DELAY);

        onView(withId(R.id.viewUsersButton))
                .perform(click());

        waitForScreen(R.id.refreshButton, 15000);

        waitForUserList();

        onView(withId(R.id.statusText))
                .check(matches(
                        withText(containsString(
                                "registered user(s)"
                        ))
                ));

        // Keep the Users page visible.
        pause(3000);

        // =====================================
        // STEP 6: BACK TO LOGIN
        // =====================================

        onView(withId(R.id.backButton))
                .perform(click());

        waitForScreen(R.id.logoutButton, 15000);

        pause(DELAY);

        onView(withId(R.id.logoutButton))
                .perform(click());

        waitForScreen(R.id.loginButton, 15000);

        pause(DELAY);

        // =====================================
        // STEP 7: INVALID PASSWORD
        // =====================================

        onView(withId(R.id.emailInput))
                .perform(replaceText(validEmail));

        pause(DELAY);

        onView(withId(R.id.passwordInput))
                .perform(
                        replaceText(WRONG_PASSWORD),
                        closeSoftKeyboard()
                );

        pause(DELAY);

        onView(withId(R.id.loginButton))
                .perform(click());

        // Wait until the login request finishes.
        waitForLoginResult();

        // =====================================
        // STEP 8: VERIFY REJECTION
        // =====================================

        onView(withId(R.id.loginButton))
                .check(matches(isDisplayed()));

        onView(withId(R.id.loginButton))
                .check(matches(isEnabled()));

        // Demonstrate the invalid login result.
        pause(3000);
    }

    // =====================================
    // READ OTP FROM VERIFICATION PAGE
    // =====================================

    private String readOtp() {

        AtomicReference<String> result =
                new AtomicReference<>("");

        onView(withId(R.id.verificationCodeText))
                .perform(new ViewAction() {

                    @Override
                    public Matcher<View> getConstraints() {
                        return isDisplayed();
                    }

                    @Override
                    public String getDescription() {
                        return "Read classroom verification code";
                    }

                    @Override
                    public void perform(
                            UiController controller,
                            View view
                    ) {

                        TextView textView = (TextView) view;

                        result.set(
                                textView.getText()
                                        .toString()
                                        .trim()
                        );
                    }
                });

        return result.get();
    }

    // =====================================
    // WAIT FOR EXPECTED SCREEN
    // =====================================

    private void waitForScreen(
            int viewId,
            long timeout
    ) {

        long deadline =
                SystemClock.uptimeMillis() + timeout;

        while (SystemClock.uptimeMillis() < deadline) {

            try {

                onView(withId(viewId))
                        .check(matches(isDisplayed()));

                return;

            } catch (AssertionError |
                     NoMatchingViewException e) {

                SystemClock.sleep(200);
            }
        }

        throw new AssertionError(
                "Expected screen did not appear: " + viewId
        );
    }

    // =====================================
    // WAIT FOR USERS FROM RENDER
    // =====================================

    private void waitForUserList() {

        long deadline =
                SystemClock.uptimeMillis() + TIMEOUT;

        while (SystemClock.uptimeMillis() < deadline) {

            try {

                onView(withId(R.id.statusText))
                        .check(matches(
                                withText(containsString(
                                        "registered user(s)"
                                ))
                        ));

                return;

            } catch (AssertionError |
                     NoMatchingViewException e) {

                SystemClock.sleep(200);
            }
        }

        throw new AssertionError(
                "Users did not load from the configured API"
        );
    }

    // =====================================
    // WAIT FOR INVALID LOGIN RESULT
    // =====================================

    private void waitForLoginResult() {

        long deadline =
                SystemClock.uptimeMillis() + TIMEOUT;

        // Let Volley begin its network request.
        pause(500);

        while (SystemClock.uptimeMillis() < deadline) {

            try {

                onView(withId(R.id.loginButton))
                        .check(matches(isEnabled()));

                onView(withId(R.id.loginButton))
                        .check(matches(isDisplayed()));

                return;

            } catch (AssertionError |
                     NoMatchingViewException e) {

                SystemClock.sleep(200);
            }
        }

        throw new AssertionError(
                "Invalid login request did not finish"
        );
    }

    // =====================================
    // VISUAL DELAY
    // =====================================

    private void pause(long milliseconds) {
        SystemClock.sleep(milliseconds);
    }

    // =====================================
    // CLEANUP
    // =====================================

    @After
    public void tearDown() {

        if (scenario != null) {
            scenario.close();
        }
    }
}
