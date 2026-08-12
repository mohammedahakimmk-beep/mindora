# Mindora

Android learning app (Math first; Languages, Chess, Music ready to expand).

## Features
- Constellation forge learning path
- Onboarding + placement test
- Multi-stage lessons (content, video, examples, practice, assessment)
- On-device AI tutor
- Energy system (resets 00:00 UTC)
- Firebase Auth (Email + Google) + Realtime Database
- **Forced auto-update** from GitHub Releases / Firebase (no “Do you want to update?” prompt)

## Build
```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Auto-update
On launch, Mindora checks:
1. Firebase `config/update` (`versionCode`, `apkUrl`, `forceUpdate`)
2. Fallback: GitHub latest release APK asset

If a newer `versionCode` exists, a blocking “Updating Mindora” overlay downloads and installs automatically.

## Release a new version
1. Bump `versionCode` / `versionName` in `app/build.gradle.kts`
2. Build APK and create a GitHub Release with the APK attached
3. Update Firebase `config/update` with the new `versionCode` and `apkUrl`
