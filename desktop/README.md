# Solo Camera Windows receiver

The Windows receiver connects Solo Camera Android phones to OBS. It displays a single camera/screen feed or a four-slot grid, serves a local clean-feed page for OBS Browser sources, and can configure OBS scenes through the authenticated OBS WebSocket server. OBS Virtual Camera can then provide video to Zoom or a browser call. The receiver does not install its own Windows camera driver and does not start an OBS broadcast for you.

The receiver project targets **.NET 10 Windows/WPF**. Keep its complete build output folder together, including `feed.html` and the QRCoder license.

## Build

```powershell
dotnet build desktop/SoloCamera.Windows/SoloCamera.Windows.csproj -c Release --configfile desktop/NuGet.Config
```

Run `desktop/SoloCamera.Windows/bin/Release/net10.0-windows/SoloCamera.Windows.exe` from its output folder. The local QRCoder dependency is under `desktop/vendor/qrcoder`. OBS Studio 28+ is required for WebSocket scene setup.

## Phone camera as a webcam

1. Put Windows and the phone on the same Wi-Fi, or connect Windows to the phone hotspot. In the receiver choose **Pair by QR** in an empty slot.
2. On the phone open **Webcam**, scan the Windows QR code and confirm the intended computer. Allow the camera connection on the phone. Repeat in additional slots as needed.
3. In OBS open **Tools > WebSocket Server Settings**, enable the server and leave authentication on. Enter its port/password in the receiver and choose **Connect OBS**.
4. Choose **Set up webcam scenes**. Pick the Solo Camera single-camera or grid scene in OBS. Start **OBS Virtual Camera** when a video-call app needs a webcam device, then select OBS Virtual Camera in that app. Select a microphone separately.

If automatic virtual-camera start reports an error, start it directly in OBS. The OBS Browser source can also be created manually with the receiver's clean-feed URL; that URL changes after a receiver restart. Use **Reconnect existing OBS feeds** to update recognized sources. Do not change sources during an active OBS recording or broadcast.

## Live phone-screen demonstration

Choose **Screen recording** on the phone and enable **Live screen demo to OBS** before starting capture. After Android's screen-capture consent, use **Pair Windows - scan QR** to connect to a receiver slot. Enable narration and/or permitted app audio before starting if needed. In OBS, check the scene picture and audio meter, then start recording or streaming with OBS. The phone also saves an MP4. See [SCREEN_DEMO.md](SCREEN_DEMO.md) for audio, presenter, annotation, USB and tested limits.

## Limits and safety

Pairing is on the local network; no cloud service is involved. The receiver uses fresh pairing codes and a session-specific clean-feed URL. Keep the feed URL private and use the receiver only on a trusted network. USB media forwarding after QR pairing requires an unlocked phone, authorized USB debugging and Android platform-tools. Four slots are implemented, but four simultaneous physical phones and long-session latency/thermal behavior still need broader validation. OBS Virtual Camera is video-only; phone screen-demo audio reaches OBS, not the virtual camera device.

The older release notes are retained in [RELEASE_HISTORY.md](RELEASE_HISTORY.md) for context. Source architecture is documented in [../docs/ARCHITECTURE.md](../docs/ARCHITECTURE.md).
