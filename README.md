# QuietAuth (Android)

A native Android **QuietAuth** — a private 2FA (TOTP) authenticator with PIN unlock, optional biometric unlock and CSV backup. Built with Kotlin, Jetpack Compose and Material 3.

Repository: **`quiet-auth`**. Application ID: **`io.quiet.auth`**.

## Stack

- Kotlin 2.1 + Jetpack Compose (Material 3)
- AndroidX Navigation Compose
- AndroidX Biometric (`BiometricPrompt`)
- CameraX + ZXing (`zxing-android-embedded`) for QR scanning
- Storage Access Framework for CSV backup import/export
- Custom AES‑GCM wrapper backed by Android Keystore for the PIN hash and the encrypted token store (no third‑party crypto)

## Project layout

```
app/
  src/main/kotlin/io/quiet/auth/
    domain/      pure Kotlin: TOTP, otpauth URI, backup CSV, PIN
    data/        SecureStorage (Keystore + AES-GCM), TokenRepository, PinRepository, BackupIO
    auth/        BiometricAuth (BiometricPrompt wrapper)
    session/     SessionLockController for backgrounding behaviour
    ui/
      theme/     Material 3 theme
      nav/       Routes + AppNav
      viewmodel/ State holders
      screens/   Compose screens (one per route)
  src/test/kotlin/io/quiet/auth/domain/   JUnit ports of tests/domain
  src/main/res/                            strings, icons, themes
```

## Routes

| Path                     | Screen                       |
|--------------------------|------------------------------|
| `/`                      | `OnboardingScreen`           |
| `/pin`                   | `PinScreen`                  |
| `/twofas`                | `TwoFAsScreen`               |
| `/token/{id}`            | `TokenDetailsScreen`         |
| `/add-2fa`               | `AddTwoFAScreen`             |
| `/add-2fa-qr`            | `AddTwoFAQrScreen`           |
| `/settings`              | `SettingsScreen`             |
| `/backup-processing`     | `BackupProcessingScreen`     |
| `/developer-mode`        | `DeveloperModeScreen`        |

## Build

You need a **JDK 17+** with `javac` on the path (or configure toolchains — see below). The Android SDK must be installed (`ANDROID_HOME`).

Open the project in Android Studio (Hedgehog or newer). On first sync, Gradle downloads the wrapper distribution from `gradle/wrapper/gradle-wrapper.properties`. Then:

```
./gradlew assembleDebug
./gradlew test
./gradlew installDebug    # connected device / emulator
```

### JDK toolchain (CLI without a local JDK)

This project uses **Kotlin JVM toolchain 17** and the [Foojay Resolver](https://github.com/gradle/foojay-toolchains) convention plugin in [`settings.gradle.kts`](settings.gradle.kts), so Gradle can **download a matching JDK automatically** when none is installed locally (requires network on first run).

If you prefer a fixed JDK, install `openjdk-17-jdk` / Android Studio’s bundled JBR and ensure `JAVA_HOME` points at it.

> If you don't have the Gradle wrapper jar yet (e.g. fresh clone before opening in Android Studio), run once: `gradle wrapper` from a host with Gradle installed, or let Android Studio create it during the initial sync.

## Compatibility with the reference TypeScript implementation

The CSV backup format follows the same column layout as the shared `domain/backup` contract used by the cross‑platform reference app, so exports remain interchangeable where that format is aligned. The TOTP implementation is verified against the same vectors used in `tests/domain/totp.test.ts` in that reference tree.

The PIN hash is stored as a SHA‑256 hex digest matching `domain/pin.ts`. The on‑disk JSON layout for tokens uses preference key **`quietauth_twofa_items_v1`**, following the same JSON shape as the historical Expo adapter (`token-repository-adapter.ts`).

## Privacy notes

- All secrets stay on the device. There is no network use at runtime.
- The token JSON and PIN hash are sealed with an AES‑GCM key residing in the Android Keystore (StrongBox is preferred when available); the ciphertext lives in plain `SharedPreferences`.
- The CSV backup is intentionally **not** encrypted; the in‑app banner reminds the user to keep the file private.
