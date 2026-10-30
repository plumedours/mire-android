<p align="center">
  <img src="app/src/main/res/drawable-nodpi/mire_brand_flat.png" width="120" alt="Mire app icon">
</p>

# Mire

**Your archery journal, one arrow at a time.**

[Français](README.md) · **English**

![Android 8.0 and later](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Published version](https://img.shields.io/github/v/release/plumedours/mire-android?include_prereleases&label=version)

**[Download the APK](https://github.com/plumedours/mire-android/releases)** · [Release notes](CHANGELOG.md) · [Report an issue](https://github.com/plumedours/mire-android/issues/new/choose)

Mire is an Android journal for archery practice and competitions. Record scores, place arrows on a target and track your progress. It works offline and requires no account.

## Features

- **Practice and competitions:** enter scores using target-coloured buttons or place arrows directly on the face.
- **Configurable targets:** colour faces, vertical and triangular triples, field, Worcester, Beursault, blank butt, Nature, 3D and balloon modes.
- **Touch aiming:** an offset crosshair and magnifier help position arrows, with zoom and pan controls.
- **Results by end:** review the full session or swipe through individual ends; overlay multiple spots around a shared centre.
- **Progress tracking:** statistics, heatmaps, goals and a session journal.
- **Equipment:** bows, arrow sets and personal setups.
- **Backup and sharing:** Volée import, full backups, PDF, Excel, CSV and result-image exports.
- **Personalisation:** French and English interfaces, several colour palettes and layouts that adapt to screen width.

Nature and 3D diagrams show neutral scoring zones without depicting a particular animal or claiming a physical scale.

## Install Mire

1. Open the **[Releases](https://github.com/plumedours/mire-android/releases)** page.
2. Choose a release and download **`Mire.apk`** from its **Assets** section.
3. Open the APK on your phone and allow installation from your file manager if Android requests it.

**Android 8.0 or later is required.** Read the release notes for tested devices and known limitations. Releases labelled **Pre-release** are test versions.

For updates, install the new APK over the previous version. You can keep an additional copy of your journal by creating a backup in Settings. GitHub’s **Source code** archives contain the project, not an installable application.

## Bring your Volée history

Export a JSON backup from Volée. In Mire, open **Settings → Import Volée or Mire**, select the file and review the preview before merging your data.

## Build the project

Open the folder containing `settings.gradle.kts` in Android Studio. The app uses Kotlin, Jetpack Compose and Room. The current configuration requires Android SDK 37 and JDK 25 for the Gradle daemon.

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

To distribute the app, generate a **signed release APK** through **Build → Generate Signed App Bundle or APK → APK**. Keep your signing key outside the repository and reuse it for updates.

- **[Create the repository and publish a release](docs/PUBLISHING.en.md)**
- [Architecture — French](docs/ARCHITECTURE.md)
- [Validation results and limitations — French](docs/VALIDATION.md)
- [Device test checklist — French](docs/RECETTE.md)

## Report a problem

Open an [issue](https://github.com/plumedours/mire-android/issues/new/choose) with the Mire version, phone model, Android version and steps to reproduce. For a crash, include the relevant Logcat error after removing personal information.

The Google Material action icons and their licence are included in [artwork/material-icons](artwork/material-icons/README.md).
