# Android screen recording

Open Solo Camera → Screen recording. Pick an output size and optionally turn on microphone narration, then tap Start screen recording. Android asks for capture approval each session. On Android versions that support it, the system picker can offer one app or the entire screen.

After approval, open the app you want to demonstrate. Stop using the Solo Camera notification's Stop & save action, Android's capture controls, or the Screen recording page. Locking the screen also stops and saves the recording.

MP4 / H.264 video at 30 fps is saved to Movies/SoloCamera/ScreenRecordings and is listed automatically in Studio → Recordings. Microphone narration uses AAC; other apps' internal audio is not captured in this version. Saved recordings can be played or added to an editing project like other clips.

The output preserves the screen's aspect ratio with encoder-aligned dimensions and a maximum long edge of 1920 pixels. The interface displays actual output dimensions. Choose the smaller size for less processing and storage. The encoder bitrate is capped at 10 Mbps; a recording is finalized at the two-hour or 4 GB limit. Rotate before starting for the desired orientation; rotation during capture can add black borders. Protected app content follows Android's capture restrictions.

Capture runs as a foreground service and does not restart automatically after process termination or reboot. Pending files from a killed session are discarded before the next recording. No screen capture starts without Android's capture approval.

Validation: the debug APK builds and 16 sizing/bitrate configurations pass. Physical-device capture, playback, microphone audio, notification stop and rotation still require a connected Android device; none was detected during this update.

Final static validation: :app:assembleDebug and :app:lintDebug passed (0 lint errors; 88 warnings across the app). Version 0.37.0 / code 41. The previous publishing title's raw font-style number was replaced with Typeface.BOLD to clear the existing lint error.

Device follow-up: screen capture was approved and recorded successfully on TECNO KM7k in 0.38.0. A 17-second, 720 × 1600 MP4 was finalized in MediaStore and decoded successfully with OpenCV. Microphone, rotation and notification-action stop still need separate device coverage.

## Presenter camera (0.40.0)

Enable **Show front camera · presenter mode** before recording. Choose the circle size (80–160 dp) and mirror preference. Android requires camera permission and **Display over other apps** permission. Presenter mode requests entire-screen capture so the camera overlay is included while demonstrating another app.

Drag the circle to move it. Tap it to expand the camera over the display's content area; tap again to return to the circle at its previous position. The recording continues in the same file. The recording notification also offers **Camera / demo**. Stop & save releases both capture and the camera. System bars and privacy indicators remain managed by Android.

The preview uses a native TextureView camera surface, without per-frame bitmap/JPEG conversion. Camera preview is capped at 720p / 30 fps where supported. No AI model or model download is required.

Validation: version 0.40.0 builds and Android lint passes. 144 geometry cases passed for circle containment, full-screen coverage, resizing and restored positions. Installed on TECNO KM7k. Live overlay recording verification requires the phone owner to enable Display over other apps.

Device validation (TECNO KM7k, Android 15): overlay permission granted by owner. Live circle and expanded front-camera views displayed successfully. A continuous 78.264-second, 720×1600 MP4 finalized with is_pending=0; frames at 5/25/40/60/75 seconds decoded. Circle and full-screen camera are present in the saved video. Stop & save removed the overlay. Manual interaction occurred during the test; drag-position restoration still needs an isolated interaction pass.

## Camera handoff and front/back presenter (0.43.0)

While screen recording with the presenter enabled, hold the circle to switch front/back. A short tap still expands/minimizes it. The screen-recording page and notification also provide front/back controls. Screen capture continues in the same file while changing lenses.

The Camera workspace and QR scanner now claim camera access before opening their camera. The presenter temporarily hides and releases its camera, then resumes after those features close. This allows demonstrations on phones that cannot open two cameras simultaneously. An unavailable presenter camera no longer aborts the screen recording. Pairing codes remain protected from screen capture; scan the physical phone display.

Validation: debug build and Android lint passed. Pure-Java tests cover overlapping camera/scanner ownership, repeated release, stale close callbacks, identity semantics, and 32 orientation cases. Installed on TECNO KM7k. A 121.430-second 720x1600 MP4 finalized with is_pending=0; frames at 5/15/35/65/110 seconds decoded. During that recording, front/back changes worked through the button and long hold; Camera workspace reached Ready, its pairing QR dialog opened, and the front presenter resumed after leaving the workspace. No AndroidRuntime crash was logged. Full two-phone pairing, QR-scanner handoff and notification action still require separate physical-device validation. Microphone was off during this test.

## Pointer, pen and highlighter (0.44.0)

Enable **Pointer, pen & highlighter** on the Screen recording page before starting. Android's Display over other apps permission is required; entire-screen capture is requested so the tools appear in the recording. No AI model, internet connection or additional video encoder is used.

The floating toolbar provides Touch (operate the app underneath), Pointer (a finger-following marker), Pen (freehand drawing/writing), Highlighter (translucent strokes), five colors, three stroke sizes, undo last stroke, and clear all. The grip moves the toolbar vertically; the double-arrow collapses/expands it. The toolbar scrolls on short displays. Pointer and drawing modes consume touches: select Touch before interacting with your demo app. Freehand writing is ink, not handwriting-to-text recognition. The toolbar itself is visible in the recording.

Marks remain when returning to Touch mode. Rotation clears the marks and restores Touch mode to avoid incorrectly positioned annotations. Stop & save removes the toolbar, ink and presenter overlay. Ink storage is bounded to 200 strokes and 12,000 points per stroke; drawing invalidates only on touch/changes, with no permanent redraw loop. Device-specific protected content may still hide overlays.

Validation: :app:assembleDebug and :app:lintDebug passed. Version 0.44.0 / code 48 installed on TECNO KM7k. Annotation toolbar, colored strokes, translucent highlighting and presenter camera appeared together on screen and in a saved 383.956-second, 720x1600 MP4 (is_pending=0). Frames at 5/290/295/310/375 seconds decoded; the 295-second frame was visually checked for recorded ink and presenter. Clearing marks, returning to Touch and Stop & save were exercised. There was concurrent phone-owner interaction, so individual undo, pen versus highlighter, pointer dragging, external-app touch-through and rotation still need isolated regression checks. No AndroidRuntime crash was logged. These checks do not establish battery or thermal benchmarks.


## Live screen demo to OBS (0.45.0)

Enable **Live screen demo to OBS**, choose microphone and/or permitted app audio, then start recording and approve Android capture. Use **Pair Windows · scan QR** with the Windows receiver Pair by QR window. One projection session feeds a GPU copy to the recorder and small JPEG screen preview; screen media uses a separate encrypted peer port (48732), leaving camera-monitor pairing on 48731. Selected audio is mixed as 48 kHz mono PCM for OBS and AAC for the phone recording. The local MP4 is finalized with audio without re-encoding its video. Live mode currently always saves a phone copy.

Windows Browser sources now accept timestamped screen and PCM packets. Audio queues are bounded, video uses the latest unsent frame, and browser playback uses a short timestamp-based buffer. OBS scene setup enables Control audio via OBS. USB forwarding is optional after LAN QR registration and requires platform-tools plus owner-approved USB debugging. See desktop/SCREEN_DEMO.md for the complete workflow and limitations.

Validation: real TECNO projection consent, GPU screen capture, captured synthetic media tone, and saved A/V MP4 passed device instrumentation three times. Encrypted USB integration passed with non-silent ordered 48 kHz packets; the isolated OBS test displayed the phone screen and registered an audio-meter peak of 0.0916. Source audio/video clocks differed by less than one second; this is not a lip-sync or end-to-end latency benchmark. The final idle integration received 46 changing-screen frames during its measurement interval (static UI updates determine frame rate). New live QR registration, microphone plus app-audio mixing, presenter plus annotation streaming, Wi-Fi latency, long-run heat/battery, and different USB microphones still need isolated device coverage. Existing local annotation/presenter recording was verified in 0.44. Android build/lint, Windows build, browser JS parse, and encrypted/audio-queue/self-tests passed.
