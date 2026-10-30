# Publish Mire on GitHub

[Français](PUBLICATION-GITHUB.fr.md) · [English](PUBLISHING.en.md)

This guide assumes the repository **plumedours/mire-android**. If you choose another owner or name, update the links in both READMEs, the changelog and these guides.

## 1. Create an empty repository

On GitHub, choose **+ → New repository**, enter **mire-android** and use the description **Carnet de tir à l’arc Android · Archery journal for Android**.

Choose **Public** for unrestricted access to the source and downloads, or **Private** for access limited to authorised repository users. Do not initialise the repository with a README, gitignore or licence: the local project already contains its presentation files.

## 2. Push the project

Open Android Studio’s terminal in the folder containing `settings.gradle.kts`:

```powershell
git init -b main
git add .
git status
git commit -m "Prepare Mire Android repository"
git remote add origin https://github.com/plumedours/mire-android.git
git push -u origin main
```

Review staged files before committing. Signing keys, personal backups, build outputs and APKs are excluded by the supplied `.gitignore`. Keep your signing key outside the project, with a separate backup. If Git requests an identity, configure your name and preferred GitHub commit email and retry the commit. Authentication may open your browser or Git credential manager.

If the folder already belongs to a repository, check `git status` and `git remote -v`; use its existing setup rather than repeating initialisation or adding the same remote again.

The default README is French, with an English link. Set the GitHub **About** description, use `https://github.com/plumedours/mire-android/releases` as its **Website**, and add optional topics. The version badge will populate after a release is published.

## 3. Match the application version and release tag

The current `app/build.gradle.kts` contains:

```kotlin
versionCode = 9
versionName = "1.4.3"
```

Use **v1.4.3** as the tag and **Mire 1.4.3** as the release title. For the next version, increase both values, for example `10` and `1.4.4`. Also update the version label currently present in `app/src/main/java/com/plumedours/mire/ui/SettingsScreen.kt` and the changelog.

Version 1.4.3 addresses a release startup crash reported on a Galaxy S21. Its signed startup has not been confirmed in this conversation. Test the signed APK on a device before labelling it stable; otherwise mark the release as a **pre-release**.

## 4. Build a signed APK

1. In Android Studio, run **Build → Clean Project**.
2. Choose **Build → Generate Signed App Bundle or APK → APK**.
3. Select your existing keystore, key alias and **release** variant.
4. Use **Locate** after the build to find the newly generated APK.
5. Install it and verify startup, a new/resumed session and backup creation.
6. Copy that tested APK into a publishing folder and rename the copy **Mire.apk**. The filename does not change its embedded version or signature.

Reuse the same key and alias for updates. The attached APK must match the source version being tagged.

## 5. Publish a release

Commit and push any changes made since the initial upload:

```powershell
git add .
git commit -m "Prepare release 1.4.3"
git push
```

If nothing changed, continue without another commit.

1. Open **Releases → Create a new release** or **Draft a new release**.
2. In **Choose a tag**, create **v1.4.3**, targeting **main**. The form can create the tag; no separate command-line tag is required.
3. Enter **Mire 1.4.3** as the title.
4. Copy [RELEASE-1.4.3.md](../.github/RELEASE-1.4.3.md), updating the tested devices and known limitations with actual results.
5. Attach **Mire.apk** and wait for the upload to finish.
6. Select **Set as a pre-release** while device validation is incomplete. For a tested stable version, select **Set as the latest release** when offered.
7. Save a draft for review or choose **Publish release** when ready.

GitHub also generates **Source code** archives. These contain the project, not an installable APK. Check that **Mire.apk** is listed under Assets.

## 6. Download links

All releases, including pre-releases:

https://github.com/plumedours/mire-android/releases

This version after publication:

https://github.com/plumedours/mire-android/releases/tag/v1.4.3

Latest stable release, once one exists:

https://github.com/plumedours/mire-android/releases/latest

Direct download of the latest stable APK, provided the attachment is always named **Mire.apk**:

https://github.com/plumedours/mire-android/releases/latest/download/Mire.apk

A specific version, including a pre-release:

https://github.com/plumedours/mire-android/releases/download/v1.4.3/Mire.apk

The `/latest` links do not serve as first-release links when only pre-releases exist. Both READMEs therefore link to `/releases`. Private repositories require authorised access.

## 7. Future versions

Update and test the application; increase the Android version code and version name; update the Settings version label and changelog; commit and push. Build a signed APK with the same key, test it, copy it as **Mire.apk** and attach it to a new release with a matching tag, such as **v1.4.4**. Keep earlier releases available.

The `.github` directory includes a bilingual release-note template and bug-report form. No signing workflow is installed; this process keeps your signing key on your computer.

## Official documentation

- [Add locally hosted code to GitHub](https://docs.github.com/en/migrations/importing-source-code/using-the-command-line-to-import-source-code/adding-locally-hosted-code-to-github)
- [Manage releases](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository)
- [Link to releases](https://docs.github.com/en/repositories/releasing-projects-on-github/linking-to-releases)
