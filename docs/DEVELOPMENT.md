# Development guide

## Prerequisites

The Android app uses Java 17, Android Gradle Plugin 8.8.2, Gradle Wrapper 8.10.2, compile/target SDK 35 and minimum SDK 29. Use JDK 17 and Android SDK Platform 35. The wrapper downloads Gradle when it is not cached. Dependency resolution uses the pinned `vendor/maven` mirror first, then Google Maven and Maven Central. Keep `app/libs` and `vendor/maven` when building from this checkout.

The Windows receiver targets .NET 10 with WPF, so it builds and runs on Windows. Its QRCoder DLL is referenced from `desktop/vendor/qrcoder` with its license nearby. OBS Studio 28+ is needed for scene setup through OBS WebSocket; OBS 32 was used during local testing. OBS itself is not bundled.

## Android build

Create `local.properties` with your own Android SDK location. It is ignored by Git. In PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug
.\test.ps1
```

The debug APK is `app/build/outputs/apk/debug/app-debug.apk`. To install on a connected Android phone, use `adb install -r app/build/outputs/apk/debug/app-debug.apk`. If several phones are connected, select one with `adb -s <serial> install -r ...`. Installation does not migrate project source media; keep originals and backups when upgrading.

`test.ps1` runs focused JVM tests for capture settings, pairing, preview transport, editor timing/tracks and other pure-Java policies. Some editor tests use `org.json` and the SDK's `android.jar`; the script prints a skip when those are unavailable. Physical-device camera, MediaProjection, gallery, and complex export checks are separate and are recorded in [VALIDATION.md](../VALIDATION.md).

## Windows receiver build

```powershell
dotnet build desktop/SoloCamera.Windows/SoloCamera.Windows.csproj -c Release --configfile desktop/NuGet.Config
```

Run the complete output folder, not just the executable. `feed.html` and the QRCoder license are copied beside it. The receiver can be self-tested with `SoloCamera.Windows.exe --self-test <absolute-report-path>`. For setup and limits, see [desktop/README.md](../desktop/README.md) and [desktop/SCREEN_DEMO.md](../desktop/SCREEN_DEMO.md).

## Configuration and secrets

The `PUBLISHING_SERVICE_URL` Gradle property is a developer build input. The production app has no publishing endpoint configured. The Android debug UI may expose a developer-only service URL, but creators should not have to configure it. A real publishing deployment needs a separate first-party service and platform approval; see [PUBLISHING_ARCHITECTURE.md](../PUBLISHING_ARCHITECTURE.md). Never store provider client secrets or refresh tokens in Android source, Gradle properties committed to Git, or the APK.

The app stores local projects, drafts and optional model/font downloads on the device. Do not use a real creator project as a test fixture. Use isolated sample projects and scrub screenshots and logcat before sharing them.

## Changes and verification

Keep camera/monitor protocol changes compatible across both installed phones, or require both to update. Test recording on the camera phone and remote control on the monitor. For editing changes, check preview, save/reopen, export and the resulting media's audio/video tracks. For screen-demo changes, verify the local MP4 and the receiver/OBS picture separately; OBS Virtual Camera is video-only. For publishing work, never label a device draft as a platform schedule until an acknowledged job exists.
