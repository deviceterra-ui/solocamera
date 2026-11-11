# Lightweight creator tools — 0.42.0

All ten requested creator tools are implemented using the existing Android and Media3 stack. No AI model, cloud service, account or new binary dependency is required.

## Camera

Open Camera → Shooting setup (sliders icon).

1. **Pre-recording check** shows camera readiness, selected microphone availability, battery, thermal status and storage. Its explicit five-second mic check measures peak level and the actual input route, then deletes the test audio. Near-clipping recording levels turn the MIC label red. This check is run manually; it does not silently listen in the background.
2. **Countdown & sound** offers Off, 3, 5 and 10 seconds, with an optional audible countdown. Press Record again to cancel a countdown. Settings are saved. The camera and connected monitor use the existing confirmed recording state indicator.
3. **Take markers**: tap the recording timer for a good moment; hold it for a mistake. They can also be added through Shooting setup. Camera-side source timestamps and microphone metadata are stored in small local sidecars. Studio reads these automatically when importing that recording.
4. **Shooting presets** save the current camera controls, resolution/FPS/quality, microphone selection/processing, guides, countdown, and teleprompter snapshot/layout. Apply a preset while stopped on its original lens. Unsupported/disconnected hardware produces an error. USB input matching uses type/name rather than relying only on a temporary device ID. Hold a preset to delete it.
5. **Social framing guides** offer original, 9:16, 1:1 and 16:9 framing. Safe areas have right and bottom clearance sliders. These are adjustable guides, not a guarantee about every platform's changing interface.
6. **Focus/exposure aids**: optional green edge peaking and zebra brightness threshold slider. Analysis runs at at most about 5.6 updates/second on a maximum 240-pixel-wide preview. It pauses at Android severe thermal status. These aids use the compressed monitoring preview; they are not sensor-RAW focus/exposure measurements. Guides are not burned into camera recordings.

Long-press the grid icon to open framing/monitoring tools directly.

## Studio

Open an editing project → Creator tools (sliders icon in the top bar). Audio also exposes voice-over and ducking controls.

7. **Voice-over** records AAC/M4A narration at the playhead with a three-second countdown, Stop, Retry and Add controls. Timeline playback is silent during narration capture to avoid speaker feedback. Added takes receive their own audio track and a narration activity envelope. Foreground-only recording stops and discards an unaccepted take when the editor closes. Recording is limited to the remaining project duration or ten minutes.
8. **Creator templates** are available in Studio's library and Creator tools. Save your current title/logo/intro/outro layout, framing, layers and video export size, then use it as a new independent project. Templates reference existing media, which must remain available on this phone. Hold a template to remove it.
9. **Music ducking**: mark voice tracks as Narration and enable Duck on music tracks. Offline amplitude analysis of the marked narration controls music volume, with reduction and fade sliders. Playback and export share the envelope. This is level-based activity analysis, not AI speech recognition; room noise in a narration recording can affect it. Analysis is requested explicitly and cancels when the editor closes or the phone gets hot.
10. **USB microphone sync / clap calibration**: select a main video, open the tool, replay a visible clap and adjust -1000 to +1000 ms in 10 ms steps. Positive values advance late audio; negative values delay early audio. Playback and export apply it; original capture files remain unchanged. For app-recorded takes, Save for this microphone setup stores the correction for future takes imported into Studio. Receiver/adapter changes can require recalibration. This is manual calibration, not automatic USB latency detection.

Studio's Take markers tool lists and jumps to imported good/mistake markers. Source-time markers remain correctly positioned through trim and speed edits. Hold a marker to remove it. They are local metadata and are not embedded in exported MP4s.

## Verification

- Android assembly and lint passed.
- Device model/DSP checks passed: independent marker copies; marker, sync and duck settings serialization; 12 PCM sync cases across mono/stereo, integer/float and advance/delay with preserved sample counts; narration trim/silence/fade envelope; export gain processing; flat-field zebra/peaking checks; new draft/library navigation.
- TECNO KM7k integration passed: actual shooting preset application, microphone capture, two markers on a camera take, marker import into Studio, voice-over recording/stop/add/activity analysis, creator dialogs, and MP4 export with sync correction and ducking enabled.
- Verified MediaStore export 36530: 1280×720, 2.389 seconds, finalized (is_pending=0).
- Live zebra and green focus peaking inspected on the phone. Template save and reuse manually verified as a separate new project.
- Long-session battery/thermal measurements, subjective audio quality, external USB microphone calibration, and paired-monitor regressions still require dedicated coverage. No battery-life or sync-accuracy guarantee is inferred from short device checks.

Final build installed and both creatorTools and creatorCapture device suites passed again. Final integration export: content://media/external/video/media/36534. Debug APK payload: 18,297,454 bytes (about 18.3 MB); this is not installed storage or RAM usage. Temporary template and template-created QA project removed. Earlier validated MP4 decoded to 67 portrait-display frames. No long-session battery benchmark was performed.
