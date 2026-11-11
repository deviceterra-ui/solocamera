# Screen demo update (0.45.0)

Live phone screen video and selected audio can now reach OBS. See [SCREEN_DEMO.md](SCREEN_DEMO.md) for setup, USB requirements and tested limits. Open the receiver from `dist/ScreenDemo/SoloCamera.Windows/` for this version. The older webcam-only notes below describe the previous release.

# Solo Camera for Windows — first webcam build

This receiver connects up to four Android Solo Camera feeds and sends a clean single-camera or 2 × 2 grid to OBS. OBS Virtual Camera provides the webcam device for compatible Windows applications. This build does not install a separate Solo Camera camera driver.

## Run

Requires Windows 10/11, .NET 10 Desktop Runtime and OBS Studio 28 or newer. Those dependencies are installed on this test computer. Open SoloCamera.Windows.exe from the complete extracted folder.

1. Put the computer and phone on the same Wi-Fi, or connect the computer to the phone’s hotspot.
2. In a Windows camera slot choose Pair by QR. On the phone choose Webcam and scan the Windows code.
3. Confirm Connect on the phone, then Allow the camera connection. Repeat in another Windows slot for additional phones. Manual pairing remains available under Manual pairing. Codes expire after two minutes and can be used once; close and reopen Pair by QR for a fresh code.
4. In OBS enable Tools → WebSocket Server Settings → Enable WebSocket server. Keep authentication enabled. Enter its port and password in the receiver, then Connect OBS.
5. Click Set up webcam scenes, then Start webcam. Choose a single camera or the grid in the receiver.
6. In Zoom or browser video settings choose OBS Virtual Camera. Keep OBS and the receiver running. Select a microphone separately.

You can also copy the clean feed URL and add it manually as an OBS Browser source at 1280 × 720. The address changes each receiver session. USB / HDMI cameras can be mixed into scenes as OBS Video Capture Device sources.

## Current limits

Video only. No microphone relay, automatic discovery, standalone virtual camera driver or macOS build in this version. One monitor client per phone. Four slots are implemented, but simultaneous four-physical-phone operation has not yet been tested. Testing over USB uses adb port forwarding; automatic USB setup is not included in the interface. JPEG streaming is intended as the first working path; it is not hardware-encoded conferencing video.

QR registration is encrypted and works without internet on Wi-Fi or a hotspot. The computer must accept incoming connections from the phone on this local network; if Windows asks, allow the receiver on your trusted private network. If the computer has multiple network adapters, choose the Wi-Fi/hotspot address below the QR code. Pairing secrets are not saved. The clean feed server listens only on this computer and uses a fresh session token. Disconnect clears the preview. The receiver does not start OBS recording or streaming. Scene setup is blocked while OBS is recording or streaming.

## Build and regression tests

From desktop: dotnet build SoloCamera.Windows -c Release --configfile NuGet.Config

Run SoloCamera.Windows.exe --self-test <absolute-report-path>. Live integration testing additionally requires the phone pairing dialog XML, adb forwarding and an isolated authenticated OBS WebSocket configuration; see IntegrationTest.cs. Test fixtures are not prerequisites for normal operation.

OBS 32.0.4 test note: the virtual-camera output passed when started by OBS at launch. WebSocket start did not activate it consistently in the isolated test. If Start webcam reports an error, click Start Virtual Camera directly in OBS. Automated start needs further validation. Zoom/browser calls and four simultaneous physical phones have not been validated.


Live-preview update (Android 0.39.0): update both Android endpoints, or Android and Windows together. Camera → Shooting setup → Preview offers Live priority (default, smaller JPEGs) and Detail priority. Recording retains its selected resolution and frame rate. The new transport acknowledges frame IDs and limits in-flight video to four frames, replacing unsent video with the newest image. Windows receive/feed tasks run outside the UI thread and OBS waits for a new frame rather than polling duplicate JPEGs. Reconnect existing OBS feeds reconnects recognized local Solo Camera Browser sources to this session, without changing scenes or stopping a virtual camera; source updates are refused during OBS recording/streaming. Frame-rate and synthetic slow-receiver tests do not measure end-to-end camera-to-display latency. This build is ready for a visual latency check; zero delay has not been established.
