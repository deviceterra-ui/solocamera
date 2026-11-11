# Solo Camera

Solo Camera is a native Android creator app with phone-to-phone camera monitoring, a teleprompter, screen recording, a multitrack video editor, and a Windows receiver for OBS. The Android app in this repository is **0.52.1 (build 58)**. It is a development build; camera capabilities and sustained performance depend on the device.

## What works

- **Camera and monitor:** record on one Android phone and frame/control it from a second phone over the same Wi-Fi or the camera phone's hotspot. Pair with a QR code and approve the monitor on the camera phone. Available ISO, shutter, focus, white balance, zoom, frame-rate, resolution and other controls follow Camera2 hardware capabilities; a fixed-aperture lens remains fixed.
- **Creator capture:** teleprompter, recording checks, framing guides, audio-route selection, take markers and camera looks.
- **Screen demos:** record the display with optional microphone, presenter camera bubble, drawing tools and supported app audio. A live screen-demo feed can be sent to the Windows receiver and into OBS; a local recording is saved as well.
- **Studio:** import media; edit video, images, text, audio and effects on separate tracks; use keyframes, masks, transitions, titles, creator presets and privacy blackout; export MP4 or audio-only files.
- **Windows receiver:** connect phone camera or screen-demo feeds, display up to four slots, and create OBS Browser sources/scenes. OBS Virtual Camera can present video to Zoom and browser calls. Screen-demo audio is routed to OBS; the virtual camera itself does not carry audio.

**Schedule & Publish currently saves local drafts.** Account connection and automatic publishing require a first-party HTTPS service, provider credentials, approvals and workers that are specified but not present in this repository. The app does not claim a local draft was published or scheduled on a social platform.

## Start here

1. Install Android Studio with **JDK 17**, Android SDK Platform **35**, and the matching SDK build tools. Open this folder as the Android project. On Windows, set `local.properties` to your SDK path, for example `sdk.dir=C:/Users/you/AppData/Local/Android/Sdk`. This machine-specific file is ignored by Git.
2. Build the debug app with `./gradlew :app:assembleDebug` (PowerShell: `.\gradlew.bat :app:assembleDebug`). Install the resulting `app/build/outputs/apk/debug/app-debug.apk` on both Android phones.
3. Put both phones on the same Wi-Fi, or connect the monitor to the camera phone's hotspot. On the tripod phone open **Camera > Pair phones**. On the other phone open **Monitor > Scan QR**, scan the camera's code and approve the request on the camera phone.
4. For Windows OBS use, build the receiver as described in [desktop/README.md](desktop/README.md). Pair its slot by QR. For a phone-screen presentation, follow [desktop/SCREEN_DEMO.md](desktop/SCREEN_DEMO.md).

Camera recording and peer monitoring work on a local network without internet. Both phones should run the same Android build. Keep the camera workspace visible during a take. Test the actual camera, microphone, network and export settings before an important shoot.

## Documentation

- [Complete illustrated creator manual](docs/manual/README.md) — creator workflows and instructions for building the PDF from source.
- [Developer guide](docs/DEVELOPMENT.md) — toolchain, builds, tests and debugging.
- [Architecture and source map](docs/ARCHITECTURE.md) — Android/Windows boundaries and data flow.
- [Feature status](docs/FEATURE_STATUS.md) — implemented behavior and current limits.
- [Windows receiver](desktop/README.md) and [live OBS screen demo](desktop/SCREEN_DEMO.md).
- [Publishing architecture](PUBLISHING_ARCHITECTURE.md), [screen recording](SCREEN_RECORDING.md), [privacy blackout](PRIVACY_BLACKOUT.md), [profession titles](PROFESSION_PRESETS.md) and [third-party notices](THIRD_PARTY_NOTICES.md).
- [Validation record](VALIDATION.md) and [historical implementation notes](docs/IMPLEMENTATION_HISTORY.md). Historical notes describe earlier builds and are not the current feature contract.

## Repository boundaries

This repository contains Android and Windows source, the pinned local binary dependencies needed by the builds, licenses and documentation assets. It intentionally excludes APKs, recordings, device captures outside the curated manual, local SDK paths, signing keys, credentials and generated QA output. Never commit OAuth tokens, live QR pairing codes, user videos or service secrets.

No project-wide license has been granted in this repository. Third-party components retain their own licenses, listed in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
