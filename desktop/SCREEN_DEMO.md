# Live phone screen demos in OBS — version 0.45

1. Open the updated SoloCamera.Windows.exe from this complete folder. Keep it running.
2. In a receiver slot choose **Pair by QR**.
3. On the phone open **Screen recording**, enable **Live screen demo to OBS**, and choose microphone narration and/or **Include app audio in live demo**. Presenter camera and drawing tools are optional.
4. Start recording and approve Android's entire-screen capture. Scroll to **Pair Windows · scan QR**, scan the receiver code, and confirm **Connect**. Pairing requires the same Wi-Fi or phone hotspot. It does not require internet.
5. In the receiver connect OBS, then choose **Set up webcam scenes** (or **Reconnect existing OBS feeds** for previously configured sources). Select the phone's slot and its OBS scene. Do setup before starting OBS recording/streaming.
6. Check the source in OBS's Audio Mixer, then use OBS's normal Start Streaming or Start Recording controls. The receiver does not start a public broadcast automatically.
7. On the phone, use **Stop & save** when finished. It ends the feed and saves a local MP4 with the selected audio.

For a manually added OBS Browser source, copy the receiver's clean feed URL, set source dimensions to the OBS canvas, and enable **Control audio via OBS**. In four-slot grid mode, audio comes from slot 1; in a single-slot view it comes from that slot. Avoid enabling a second copy of the same phone feed in the same scene, which can double its audio.

## USB feed

Before pairing, check **Use USB feed after QR pairing** in the receiver. Connect one unlocked Android phone by USB and authorize USB debugging. Android platform-tools/adb must exist under the user's Android SDK directory. QR registration still uses Wi-Fi/hotspot; after registration, the media connection uses USB. Closing the receiver removes its USB forwards. This is a developer-tool USB bridge, not a driverless consumer USB implementation.

## What is included

Screen video, the presenter bubble/full-screen presenter, annotations, microphone narration and capturable app audio travel through the authenticated encrypted phone connection. Microphone and app audio are mixed into mono 48 kHz PCM for OBS. AAC is also saved into the phone MP4. The live preview is capped at a 720-pixel long edge and up to 15 fps; the saved phone recording retains its selected dimensions. Screen updates determine actual live frame rate. Timestamp-based playback uses a short audio/video buffer; it is not zero latency.

Android can exclude protected video and apps that prohibit playback capture. Calls, communications audio and arbitrary system sounds are not promised. Local recording is currently always enabled in live mode. Zoom/browser webcam audio is separate: OBS Virtual Camera exposes video only, so this update's phone audio is intended for OBS recording/streaming.

## Tested

TECNO KM7k → USB → Windows receiver → OBS Browser source: real screen rendered, non-silent captured media tone reached the OBS audio meter (peak 0.0916), encrypted 48 kHz packets remained ordered, and sampled source clocks differed by less than one second. Phone-side saved MP4 contained audio and decodable video. Android build/lint and Windows build/self-tests passed. These tests do not establish measured end-to-end latency, voice lip-sync, long-session thermal behavior, four-phone audio mixing, or all USB microphone compatibility. Wi-Fi QR registration uses the existing rendezvous with the new screen port; that specific phone screen pairing flow still needs an isolated end-to-end device check.
