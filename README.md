# CalmAuth (Android)

A mindful, fully native Android port of CalmAuth — a private 2FA (TOTP) authenticator with PIN unlock, optional biometric unlock and CSV backup. Built with Kotlin, Jetpack Compose and Material 3.

This repository is the production Android target. The original cross‑platform Expo source lives at `calmauth-expo` and remains the reference implementation for iOS/web; it is not used at runtime here.

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
  src/main/kotlin/dev/calmauth/
    domain/      pure Kotlin: TOTP, otpauth URI, backup CSV, PIN
    data/        SecureStorage (Keystore + AES-GCM), TokenRepository, PinRepository, BackupIO
    auth/        BiometricAuth (BiometricPrompt wrapper)
    session/     SessionLockController for backgrounding behaviour
    ui/
      theme/     Material 3 theme
      nav/       Routes + AppNav
      viewmodel/ State holders
      screens/   Compose screens (one per route)
  src/test/kotlin/dev/calmauth/domain/   JUnit ports of tests/domain
  src/main/res/                            strings, icons, themes
```

## Routes (mirroring `calmauth-expo` paths)

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

Open the project in Android Studio (Hedgehog or newer). On first sync, Android Studio will download the configured Gradle distribution declared in `gradle/wrapper/gradle-wrapper.properties`. Then:

```
./gradlew assembleDebug
./gradlew test
./gradlew installDebug    # connected device / emulator
```

> If you don't have the Gradle wrapper jar yet (e.g. fresh clone before opening in Android Studio), run once: `gradle wrapper` from a host with Gradle installed, or let Android Studio create it during the initial sync.

## Compatibility with `calmauth-expo`

The CSV backup format is bit‑for‑bit compatible with the Expo build's `domain/backup.ts`, so users can export from one and import into the other. The TOTP implementation is verified against the same vectors used in `tests/domain/totp.test.ts`.

The PIN hash is stored as a SHA‑256 hex digest exactly as in `domain/pin.ts`. The on‑disk JSON layout for tokens (key `calmauth_twofa_items_v1`) matches `adapters/expo/token-repository-adapter.ts`.

## Privacy notes

- All secrets stay on the device. There is no network use at runtime.
- The token JSON and PIN hash are sealed with an AES‑GCM key residing in the Android Keystore (StrongBox is preferred when available); the ciphertext lives in plain `SharedPreferences`.
- The CSV backup is intentionally **not** encrypted; the in‑app banner reminds the user to keep the file private.
