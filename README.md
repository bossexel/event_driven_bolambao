# event_driven_bolambao

Android application for account registration, login, verification, and viewing registered users.

The PostgreSQL Insert API with Supabase lab is in [docs/supabase-lab.md](docs/supabase-lab.md), with the customer table SQL, importable Postman requests, and setup instructions.

Live verification on project `dijglmwapgewnqztbnis` passed: **4 requests, 8 assertions, 0 failures**. See the [live test report](docs/supabase-live-results.md). The configured Postman environment is saved locally at `postman/supabase.local.postman_environment.json` and excluded from Git.

- Android application ID and Java package: `com.example.eventdrivenbolambao`
- Minimum Android version: Android 7.0 (API 24)
- Target Android SDK: API 35

Open this directory in Android Studio and sync Gradle. Build the debug APK with:

```powershell
.\gradlew.bat assembleDebug
```

The app currently connects to `https://users-api-exiel.onrender.com`. This repository contains the Android client; hosting and ownership of that API are managed separately.

Run the login automation on a connected Android device or emulator using an account you control on the configured API:

```powershell
.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.testEmail=YOUR_TEST_EMAIL" "-Pandroid.testInstrumentationRunnerArguments.testPassword=YOUR_TEST_PASSWORD"
```

The login automation is skipped when test credentials are omitted. Keep credentials out of source control.
