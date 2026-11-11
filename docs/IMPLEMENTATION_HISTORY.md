# Solo Camera 0.20.0 · Android developer preview

A native Android application with Camera and Monitor roles, remote camera controls, local video recording, and a teleprompter. Android 10 (API 29) or later. This is an initial implementation, not a production release or the entire product roadmap.

## Install and try

Build with Android Studio or install `app/build/outputs/apk/debug/app-debug.apk` on **both phones**. This is a debug-signed development build. Android may ask you to allow installation from the app opening the APK.

1. Connect both phones to the same Wi-Fi network, or connect one to the other's hotspot. Internet access is unnecessary. Guest networks with client isolation may block pairing.
2. On the tripod phone choose **Use as camera** and grant Camera and Microphone permissions. The microphone permission is required to record video with sound.
3. Tap **Pair phones** on the camera phone. Use **Set up camera hotspot** if needed. On return it shows a fresh private pairing QR code.
4. On the handheld phone choose **Use as monitor**, connect to the camera hotspot using the provided settings button, then **Scan QR code**. Approve the request on the camera phone. Manual address/key entry remains available.
5. Tap an **ISO, shutter, focus, WB, zoom, EV, iris, or lens tile** on either phone. An on-screen adjustment dock opens while preview remains visible. Slide to apply live, or tap a quick preset. Auto/Manual and lock controls appear beside the relevant setting. Tap the selected tile again or its close icon to dismiss the dock. Camera controls no longer require numeric text entry. Tap the preview to focus where supported.
6. Tap the **teleprompter icon** in the top bar, then the **script icon** inside the prompter to write/paste a script and save it under a title. Use **Open saved** to reopen scripts. Prompter settings provide speed and text-size sliders; the play/pause, restart, next paragraph, and mirror actions use icons. Place the monitor close to the filming lens for a natural eye line.
7. Open the top-bar **settings icon** and choose **Scroll when recording starts** if desired. Settings also offer an optional recording countdown. Tap the round red record button on the monitor; scrolling starts after recording is confirmed. The button becomes a stop square only after the camera confirms recording. Pausing the script leaves video recording running.
8. Stop recording. Confirm **Saved to Movies/SoloCamera** on the camera or monitor. Videos are stored on the camera phone and are visible to gallery apps.

The camera app must remain visible. Backgrounding it, locking its screen, or leaving camera mode ends an active take and attempts to save it. Losing just the monitor connection leaves camera recording running while the camera app stays open. Reconnect manually using Pair; no command retries are sent automatically.

## Implemented

- Preview-first native camera workspace. All ten camera tiles are visible in a two-row portrait grid and a single landscape row. Portrait adjustments appear above the tiles; landscape adjustments open beside the preview to preserve its height. The teleprompter is hidden initially and remains one tap away.
- Original vector icons, amber active controls, circular recording control, compact status, composition grid, focus reticle, and direct torch/stabilization icons. Icon actions have accessibility descriptions and long-press tooltips.
- ISO, shutter, focus, zoom, exposure compensation, WB presets, and adjustable aperture use capability-based sliders and presets. ISO, shutter, and zoom use logarithmic ranges; exposure compensation displays actual EV units. Unsupported or fixed controls explain their hardware limit.
- Slider commands are coalesced to approximately one every 80 ms while moving, with a final value sent on release. Requested values appear immediately with an applying indicator; camera state reconciles them. Camera capture metadata updates roughly five times per second. Moving from auto exposure to manual carries the other current exposure value before validation.

- Single camera phone and single approved monitor over local TCP, port 48731.
- Authenticated AES-GCM packets, fresh connection keys derived from a random server challenge and a random 128-bit pairing secret, directional sequence nonces, bounded packet lengths, and explicit camera-side approval. Revoke a monitor and rotate the key through the camera's Pair dialog.
- Camera2 capture with separate video and JPEG preview paths. Preview targets up to 30 frames/second with a smaller, aspect-matched image; the recorder uses the selected supported resolution, frame rate and quality-scaled H.264 bitrate, with AAC sound. Recording defaults to a supported format at or below 1920×1080. Unsupported simultaneous streams fail explicitly.
- Preview and recording aspect ratios must match. Preview stays out of the recorded video path.
- Explicit start/stop commands, bounded duplicate-command history, confirmed recording state, countdown with cancellation, battery/storage/thermal status, and approximately once-per-second audio peak status during recording.
- Capability-dependent ISO, shutter time, automatic exposure and compensation, exposure lock, white balance presets and lock, continuous autofocus, tap-to-focus, manual focus in diopters, digital zoom, torch, lens switching, electronic or optical stabilization, and variable aperture where exposed by the device. Fixed aperture is read-only. Focus and lens naming reflect Android capabilities rather than promises about every physical lens.
- Camera mutations run on one capture handler. Stabilization and lens changes are blocked during recording. Manual shutter is constrained to the selected frame rate exposure budget.
- Named scripts stored locally, text editing, play/pause, speed, font size, paragraph navigation, restart, and text mirroring for a reflector.
- Script beside the preview or translucent over it. Script overlays never enter captured video.
- Optional camera-side prompter display: open the **settings icon** on each phone, enable **Allow monitor to display a script here** on the camera and **Send script to camera screen** on the monitor. The camera screen must face the creator to be useful. Remote scripts remain session-local unless saved explicitly on the camera.
- Script commands include full script content and revision and are delivered in channel order. Changes, play/pause, and position jumps synchronize when the operator changes a control. Continuous cross-device position correction is not implemented; fonts and screen sizes can produce different reading layouts. Reading on the monitor is the recommended mode.
- Direct recording to a pending MediaStore entry avoids making a second full-sized copy. **Recover interrupted takes**, in camera shooting settings, publishes app-owned pending videos only if their duration metadata is readable. It cannot repair unfinished MP4 metadata; unreadable pending entries are retained for Android to expire.

## Build

Use JDK 17, Android SDK Platform 35, and SDK build tools. The standard Gradle wrapper pins Gradle 8.10.2; Android Gradle Plugin is 8.8.2. Dependencies and tools need downloading on a clean machine.

Open this directory in Android Studio, configure the local SDK path, and build, or run:

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug
.\test.ps1
```

Create `local.properties` with your own SDK path if needed, for example `sdk.dir=C:/Users/your-name/AppData/Local/Android/Sdk`. Do not commit this file. `JAVA_HOME` must point at a JDK supported by Gradle; the machine's default JDK 24 is not appropriate for this pinned toolchain.

ZXing Core is bundled for local QR generation and scanning. The initial UI uses Android platform views and Java instead of the proposed Kotlin/Compose stack, allowing the available cached toolchain to build the app without downloading a UI framework. A future UI migration can retain the transport and capture boundaries.

## Source map

- `MainActivity.java`: roles, permissions, pairing approval, adaptive preview workspace, remote command dispatch, lifecycle, and recording-linked scrolling.
- `CameraControlsView.java`: icon/value tiles, adaptive adjustment dock, live sliders, quick presets, and pending-value reconciliation.
- `CameraIcon.java`, `RecordButton.java`, `PreviewGuides.java`: native vector icons, confirmed record/stop control, composition grid, and focus reticle.
- `SliderScale.java`: hardware-bound linear/logarithmic slider mappings.
- `CameraEngine.java`: capability discovery, Camera2 requests, preview conversion, recorder/session lifecycle, status, and gallery finalization/recovery.
- `PrompterView.java`: script library, reading UI, scrolling, and optional remote snapshots.
- `PeerLink.java`: local socket lifecycle, heartbeat, approval, bounded controls, and latest-frame delivery.
- `SecureChannel.java`: pure Java authenticated packet transport.
- `tests/SecureChannelTest.java`: real loopback socket tests for bidirectional delivery, wrong keys, oversized packets, tampering, replay, and generated key encoding.
- `tests/SliderScaleTest.java`: logarithmic endpoints, monotonicity, round trips, fixed ranges, invalid metadata, and shutter exposure limits.

## Validation and release limits

See `VALIDATION.md` for actual checks and the physical-device test procedure. Compiling and passing lint do not establish camera hardware compatibility. The redesigned portrait workspace and live camera preview have been visually checked on a Realme RMX3085 (Android 13). The final icon-size fix still needs reinstalling after that phone reconnects; controlled landscape, recording, and two-phone checks remain pending.

This build does not implement automatic peer discovery, WebRTC, background recording, automatic reconnection, clip transfer/review, configurable codecs, calibrated Kelvin/tint, microphone routing or gain, voice-following scrolling, focus peaking, zebras, histogram, LUTs, log/RAW video, or multi-camera monitoring. It has an English platform-view UI; accessibility, small-screen/landscape layout, translations, sustained performance, and thermal behavior require device testing and refinement. Network encryption is implemented and tested at the packet layer but has not had an independent security review.

The product plan in the parent folder remains the roadmap. Its timeline describes the full release, not completion of this developer preview.


## QR pairing (v0.3.0)
On the camera phone, open Pair phones to display the QR code. On the monitor phone, open Pair phones and tap Scan QR code; grant camera permission, point at the code, then approve the monitor on the camera phone. Both phones must share Wi-Fi or a hotspot. If several camera addresses appear, select the Wi-Fi/hotspot address. Manual entry remains available. Revoke / new code invalidates the previous key. Keep QR codes private.

QR scanning runs locally using ZXing Core 3.5.3 (Apache-2.0); the pinned JAR and license are in app/libs. No external scanner app or Google Play services is required.


## Hotspot pairing (v0.4.0)
Use the main/camera phone as hotspot host. In camera Pair phones, tap Set up camera hotspot and enable it in Android settings. Connect the monitor using Connect to camera hotspot, return to the app, scan the refreshed code, and approve on the camera phone. A router, mobile data and Internet service are not required; accept Stay connected if Android warns about no Internet. Set up networking before recording.

The monitor binds its connection to a Wi-Fi network socket, preferring the subnet matching the camera address, rather than following a cellular default route. Camera pairing addresses exclude cellular, VPN, and USB interfaces and prioritize hotspot interfaces. Changing hotspot settings regenerates the pairing session on return. Connection failures and stalled links now appear directly over the preview.


## Fine white balance (v0.5.0)
Open the WB tile and slide Cool / Warm to adjust white balance continuously. Presets remain as quick choices. The first adjustment freezes the current camera white point and applies relative red/blue gain changes; green gains and the camera color transform are retained. Reset returns to that frozen white point. Choose Auto or another preset to leave manual white balance and acquire a new baseline. Controls work locally and from the monitor using the existing encrypted command path, and affect capture requests for both preview and recording. Install the update on both phones.

The scale is relative (-100 to +100), not calibrated Kelvin. Manual fine adjustment requires Camera2 MANUAL_POST_PROCESSING, AWB OFF, gain/transform request keys, and captured gain/transform metadata. Unsupported lenses keep presets available and disable the fine slider. Lens changes reset the baseline.


## Camera flip and video quality (v0.6.0)
Tap the camera-flip icon beside the recording button to switch between front and back cameras. It also works from the monitor. The Lens tile retains access to individual camera IDs. Finish recording before switching camera or video format. White balance and exposure controls reset for a new lens; each lens remembers its selected video format during the camera session.

Tap the gold video format badge, or Shooting setup → Video resolution / quality. Choose a reported resolution/frame-rate combination and Standard or High bitrate. Maximum available selects the largest reported compatible option with High bitrate. Options intersect camera output sizes/timings, regular-session 30/60 fps ranges, matching preview aspect ratios, and hardware H.264 encoder size/rate/bitrate limits. This can expose 4K on devices whose Android Camera2 pipeline supports it; it cannot unlock formats withheld by the device. Simultaneous recording and preview must still succeed on the actual hardware. Unsupported recording combinations report an error and restore preview. The transmitted JPEG preview remains lightweight; higher quality applies to the locally saved recording.

On the tested TECNO KM7k, the rear camera exposes up to 2560 x 1440 at 30 fps. Saved test video metadata confirmed 2560 x 1440; front/back switching and live preview were also checked. Other devices and sustained recording still require validation.


## Preview pacing, resolution names and USB microphones (v0.9.0)
Removed the original 180 ms preview throttle and the transport writer 80 ms polling delay. Camera YUV conversion/JPEG encoding now runs on a dedicated preview thread; JPEG decoding runs on a separate bounded decoder, away from the camera handler and network reader. Preview uses a smaller matching size (up to 960 x 720) and a 33 ms pacing interval; it drops old frames rather than queuing stale video. Actual rate depends on the phone, network and thermal state. Tap camera status for measured rendered preview fps. This is still JPEG transport, not WebRTC; it is not a guarantee of constant 30 fps.

Resolution names now include 720p, 1080p, 1440p / 2.5K, 2K DCI, 3K, 4K UHD/DCI and 8K where the exact size is reported. Other aspect ratios retain exact dimensions. No unavailable size is invented.

Tap MIC or Shooting setup → Microphone input / USB receiver to choose an input on the camera phone. Recording checks the actual routed input, rejects an unavailable selected device, and stops/saves the take if the route changes mid-recording. This prevents silent switching to the built-in microphone; it does not measure or remove a USB receiver's internal processing delay. MediaRecorder still captures audio/video together with its native timestamps. Validate synchronization with a visible/audible clap at the start and end of a take using the exact receiver. A constant offset and increasing drift need different fixes; do not assume preview latency is recorded lip-sync error.


## v0.9.0 creator controls
Tap **FPS** in the live HUD. The slider snaps to supported whole-number recording rates from 10 fps upward; quick chips include 10, 24, 25, 30, 50 and 60 when available. Release the slider to apply. This changes recording cadence, not network preview quality. Stop recording before changing FPS. Camera stream timing, the hardware encoder and manual sensor capability limit availability. Auto exposure uses only a camera-advertised FPS range; other rates switch to manual exposure using the latest ISO and shutter. Adjust ISO/shutter for the scene, or choose an automatic FPS preset before returning exposure to Auto. Resolution or lens changes restore that format's default FPS.

Tap **LOOK** for Natural, Cinema, Vlog, Warm, Cool or Fade. Slide intensity while watching the image. Supported cameras apply the RGB tone curves before both preview and video recording, so the look is baked into the saved clip. Natural restores the camera's normal tone mapping. These are creative looks, not calibrated color profiles, Log recording, HDR or LUT import. Unsupported cameras explain the limit and keep Natural available.

Tap **MIC** for microphone input and optional **Voice processing**. This requests Android's voice communication capture mode; actual noise reduction and automatic gain behavior depend on the phone, route and receiver. Compare test takes with it on and off. It is not AI denoising, does not guarantee removal of background noise, and provides no hardware microphone gain control. Lower gain on the external receiver if input clips. No AI audio cleanup service is integrated. The existing recording route checks remain active; USB lip-sync still needs a real receiver test.


## v0.9.0 playback and editor
Open **Playback / editing** on the home screen, or the film icon in the camera/monitor toolbar. Stop an active take first. The editor is a separate Activity with no camera HUD; camera and monitor resources close when it opens. The camera icon returns to shooting. Library and Editor buttons switch between the separate screens.

The library lists this app's saved recordings and exports on the current phone. Tap a recording to play, pause and seek, or choose **Edit this video**. **Import videos** uses the Android document picker without requesting broad media-library access. Camera-phone recordings are not automatically copied to the monitor. Imported files stay in their original location; keep them available and preserve the document permission.

The editor supports up to 30 sequential video clips per project. The timeline selects clips; the playhead seeks across the project. Its toolbar provides Add, Trim, Split, Speed (0.25x–4x), Volume (0–200%), Looks with intensity, brightness/contrast/saturation, captions with size/color/position, original/9:16/16:9/square canvas with fit/fill, imported looping music with level, rotate, mirror, reorder, duplicate and remove. A project keeps at least one clip. Trim sliders and playhead In/Out actions preserve at least 0.2 seconds; splitting operates at the playhead in source time after speed adjustment. Original recordings are never edited or deleted by these tools. Volume above 100% can cause clipping; there is no AI noise removal in the editor.

Edits automatically save to private project files with atomic writes. Undo/redo stores up to 40 steps during the current editor session. Saved projects reopen from Library; undo history does not persist after closing the editor. Video, audio and caption effects use the same Media3 composition for preview and export. Slider changes update the preview after a short idle period or release; rebuilding a preview pauses playback. Music starts at project time zero and loops to its end. Captions apply for their whole clip, with long lines wrapped automatically. There are no keyframes, transitions, automatic captions, stickers, voice effects, face smoothing, LUT import, AI models or cloud services in this version.

Export renders H.264/AAC MP4 at 720p or 1080p (short edge for portrait, height for landscape) with supported hardware fallback. Export may lower dimensions if required by the encoder. Results save under **Movies/SoloCamera/Exports** only after the temporary video is readable and copied completely. Play and Share actions are available after saving. Keep the editor visible and unlocked: leaving it, locking the phone or rotating during export cancels the operation and keeps the draft. Export is not a background service. A large clip can need substantial temporary storage and battery.

Media3 1.8.0 and its dependencies are vendored from Google Maven/Maven Central for offline builds; see THIRD_PARTY_NOTICES.md and tools/vendor_media.py. CompositionPlayer is an experimental library API, so OEM playback/export compatibility and performance still require device validation. This is an initial creator editor, not full CapCut feature parity. Local AI integration is separate future work and requires an appropriate model, license, inference pipeline and device testing.


## v0.10.0 timeline editor
The editor now uses a time-scaled filmstrip with a fixed white playhead, time ruler and separate video, caption, PIP and imported-audio rows. Drag the timeline beneath the playhead to scrub; pinch or use + / − to expand time. Tap a clip to select it and use Split at the white line. Playhead-based trim actions replace source-time sliders. Up to three muted video overlay tracks can be imported at the playhead, positioned by dragging on the preview, resized, made translucent, split or removed. Overlay timing is adjusted with Start/End at playhead; the longest video, overlay, text layer or explicitly trimmed audio end determines project duration. Shortening a video leaves other tracks intact; uncovered tail areas use a black canvas.

Tap the purple caption row to edit its text, size and color, and drag its highlighted positioning target on the preview. Text layers now have independent project-time start/end points and can be split, positioned and edited separately from video. Existing per-clip captions migrate automatically. Up to 30 text layers are supported; arbitrary animation keyframes are not implemented. The library now presents takes as thumbnail tiles. Layout respects system bars and the landscape tool pane scrolls.

Transition offers Cut and 0.3 / 0.6 / 1 second dip-to-black fades at both ends of the selected clip, capped to half its duration. These do not overlap adjacent clips or implement cross-dissolve. Looks and color adjustment remain available. Voice adds local pitch presets (Original, Deep, Low, High, Chipmunk) and fine pitch adjustment while retaining duration; volume, mute, speed and looping imported music remain available. There is no AI noise removal, speech enhancement, voice cloning or voice model. Video overlays, text placement, fades and pitch processing are included in the export composition, not just UI decorations. Preview changes pause playback and apply after release or a short idle delay.

This update implements these editing workflows, not every feature in CapCut. Large multi-stream videos depend on device decoder capacity and thermal limits; three overlay tracks have not been certified on every phone.


### Track selection and framing
Tap a video, purple text item, PIP item or green audio item to select that track; the amber outline and tool heading show the target. Trim, Split and Remove stay on that selected track. Text and imported audio have their own Start/End at playhead controls. Video and PIP trim uses source time (including speed changes). Imported audio is one looping source with independent start/end; splitting it into multiple independent music clips is not implemented.

Pinch directly on the video preview to resize the selected video/PIP (or selected text); drag to reposition it. Transform also provides frame size and reset controls. Crop trims horizontal and vertical edges symmetrically. Mask provides None, Ellipse, Rectangle and Linear with adjustable feather; a masked PIP reveals the lower video. These are centered geometric masks, not freehand masks, subject segmentation or motion tracking. Animate provides Zoom in, Zoom out and Pan right for video/PIP/text over the item's duration. No keyframe editor is included. Geometry and masks use the same GPU pipeline for preview and export. Each effect applies only to the selected item; canvas ratio remains a project-wide setting.


### Reproducing editor checks
`test.ps1` includes track-isolation and legacy/new draft serialization checks. It uses a cached JVM org.json JAR or `EDITOR_JSON_TEST_JAR` for those checks; it prints an explicit skip if unavailable. For device checks, build `:app:assembleDebug :app:assembleDebugAndroidTest`, install both APKs, then run `tools/device-editor-check.ps1 -VideoUri content://media/external/video/media/ID -DurationMs DURATION` with a video owned by this app. The runner uses a separate QA project, exercises the actual track toolbar buttons and pinch handler, and exports a short masked/animated MP4. It does not modify source media or existing user drafts. QA exports and the separately named QA draft can be removed after inspection. Test runners are included only in the separate Android test APK.


## Editor update 0.11.0
- Overlay preview sequences span the project instead of using a short looping primary sequence. New overlays initially fit the remaining main timeline; trim them independently afterward.
- CompositionPlayer 1.8.0 is vendored with a 4 MiB / 2.5 second buffer budget per sequence. The original Apache-licensed source is in app/src/main/java/androidx/media3/transformer/CompositionPlayer.java; the accompanying local AAR excludes that class and its nested classes. Export Transformer is unchanged.
- Text-track taps select captions. Text uses continuous object zoom and supports independent width/height stretching, crop, masks and feathering in preview and export.
- Preview objects have a resize handle at the bottom right. Pinch scales uniformly; the handle stretches width and height independently. Drag preserves the initial finger offset. Preview changes commit at gesture release to avoid destroying decoders repeatedly during a drag.
- Up to three video overlays remain supported; each has its own selectable PIP row, trim, transform, crop, mask and animation. This is not unlimited-track or full CapCut parity.

Overlay projects use a 360-pixel-short-edge cached preview rendered asynchronously by Transformer, then played with ExoPlayer. This avoids experimental CompositionPlayer multi-input frozen frames on the tested TECNO. Edits invalidate the cache; preparation time depends on project length and phone speed. Export retains the selected 720p/1080p canvas settings. Single-video projects continue using direct CompositionPlayer preview. Text layers each have their own timeline row. Selected video, PIP and text rows expose independent edge trim handles.

The same buffer budget applies to the vendored ExoPlayerAssetLoader used by Transformer, including export. Muted overlays do not generate audio tracks.


## Editor update 0.12.0 — live editing and replay
This release replaces the 0.11 cached-movie preview described above. The editor now decodes visible source layers directly into a fitted canvas; transforms, crop, feathered masks, captions and animation update without rendering a temporary movie. Export still uses Transformer. Off-screen tracks release their decoders so sequential overlay segments do not exhaust the phone's decoder resources. The timeline scrolls vertically when tracks exceed the available space.

The project clock pauses at the project end and Play restarts from zero with fresh decoders, avoiding a stalled end-state seek on the tested TECNO. All visible videos pause together while a source buffers. Decoder failures pause playback and report an error while keeping editing controls available. Clip changes can still incur a short decoder/seek delay; source resolution and simultaneous streams remain limited by the device. Preview color is a display approximation of export GPU effects, and preview source gain is capped at unity. Precise audio synchronization, three simultaneous high-resolution overlays and thermal behavior need broader device validation.


## Editor update 0.13.0 — deletion and timeline placement
Hold the body of a timeline item until it highlights, then drag horizontally to change its start time. The amber ghost and millisecond timestamp show the pending drop. Clip edges and the playhead attract nearby drops; pinch to zoom for finer placement. Moving near the horizontal edges scrolls time, and moving near the visible vertical edges scrolls the track list. Release commits one undoable edit; cancelling leaves the item unchanged.

Video clips can move between the main row and PIP rows, including the + PIP row; dragging between PIP rows changes their stacking order. The current limit is three PIP items, with one item per row. Main clips cannot overlap on the main row: an occupied drop moves to the next free position. Use a PIP row for overlap. Main clips can leave empty gaps; preview and export show black in uncovered areas. Captions and the single music item can also move in time; captions can change their row order. Moving a music item preserves its currently visible duration.

Deletion now reconciles preview layers synchronously, and the renderer checks the current layer counts before drawing. Main/PIP/text deletion remains undoable. Removing the last visual item returns to Library with an Undo action. Projects with only PIP or text remain editable on a black canvas. The editor is still limited to the documented local editing features and device decoder capacity.


## 0.14 editor update

Video and text rows grow when items are added (300 items per collection safety bound). Long-press a clip and drag horizontally to change its timestamp or vertically to another row. Multiple non-overlapping video items can share a row. Pinch the timeline or use its plus/minus controls to zoom. Tap the junction icon between adjacent main clips to choose a cut or a timed dip to black.

Select text, open Text, and scroll the control panel for system font families, bold/italic, alignment, size, RGBA colors, background, outline, shadow and line spacing. Select Keyframes to add position, scale, rotation and opacity keys at the playhead. Tap timeline diamonds to edit keys, choose an easing preset or drag the two curve handles. Preview and export share keyframe interpolation and text bitmap rendering.

Preview reuses video decoders for active overlays and short gaps. Export groups sequential overlay clips by track instead of allocating an input for every clip.

Limits: simultaneous video layers depend on the phone's decoder capacity. This release has cut/dip-to-black transitions, not a full transition library. Still-image import, AI effects, automatic motion-key creation from preview gestures, and preserving animation timing through every trim/split are not complete. Keyframe transforms should be adjusted through Keyframes after trimming. System font availability varies by phone.


## 0.15 editor interaction update

- Text background and shadow have independent switches. New text has both off. Background fits the text, with padding and corner sliders. Shadow has color/opacity, softness and X/Y offset controls. Color swatches apply immediately; the custom RGBA picker previews live and Cancel restores the original color.
- Font selection is a drop-down with previous/next arrows. Changes render live. Eight Android system font families are offered; available glyphs depend on the device.
- Undo and Redo are labeled below the preview. The diamond beside playback adds the first keyframe at the playhead. Once animated, dragging, pinching, using the frame-size slider or rotating creates/updates a keyframe at the new time. The Keyframes inspector still provides precise position, scale, rotation, opacity and curve controls. Stretch-width/height and mirror remain clip-wide settings.
- Image imports are decoded off the UI thread, bounded to 2048 pixels on the longest side, and copied into app storage. Images use object layers with independent timing, crop/mask, transforms and animation. The sticker picker offers twelve built-in symbols/emoji; custom transparent PNGs can be imported as images.
- Dragging below the video row creates an overlay track; drag text/image/sticker objects to the + LAYER row to create another object track. The timeline scrolls vertically to expose additional rows.
- Junction transition picker offers Cut, Dip to black, Dip to white, Zoom through and Slide left, with 0.1–1.5 second duration (capped to half each clip). These are cut-centered effects, not overlapping cross-dissolves.

Current limits: start a project with a video before adding images. Animated image formats import as a still. AI effects and a large downloadable sticker/transition catalog are not included. Animation timing across trims/splits still requires manual keyframe adjustment. Earlier version sections describe historical behavior superseded above.


## 0.16 audio and creative tools update

- Audio imports are copied from the selected file provider into app storage on a worker thread and inspected for a decodable audio track. MP3, WAV and M4A are device-tested. Other formats depend on Android codecs; an unsupported file produces an error instead of a broken timeline item.
- Each import creates an independent audio row. Tap to select; drag its edges to trim; position the playhead and Split; long-press to move in time or between audio rows. Volume, pitch, removal and undo are per item. Sequential and simultaneous audio items are mixed on export. Old music drafts migrate to audio items. The safety limit is 300 audio items, with practical simultaneous playback limited by the phone.
- Media opens Videos, Photos and Audio tabs with import actions and reusable assets currently on the project timeline. Photos import as independent overlay objects. This is a project-media view, not a permanent asset bin after items are deleted.
- Inter, Montserrat, Poppins, Bebas Neue, Playfair Display and Lora are bundled for offline text editing and export. Their SIL Open Font Licenses and source hashes are included in assets/fonts.
- Animation, transition and easing choices have animated preview cards. Existing cut, black/white dip, zoom and slide transitions remain cut-centered effects, not overlapping cross-dissolves. Easing cards supplement the editable curve handles.
- Masks have independent X/Y, width, height, rotation and feather settings, saved in drafts. Drag the preview to move a mask, pinch to resize, and use sliders for precise adjustments. Main-video masks render uncovered areas black in MP4 output; overlay masks retain transparency.

No AI model is included. RNNoise is a potential on-device speech-denoising component, but needs native integration, model/license review and device benchmarks. It does not by itself provide speech restoration, face smoothing or voice conversion. Preview gain above 100% remains capped while export supports amplification; use moderate levels to avoid clipping when mixing sounds. The device checks use generated local audio fixtures; third-party cloud file providers and every codec variant are not certified.


## 0.17 transition collection

Tap the junction icon between adjacent main-video clips to open the animated three-column gallery. Select a preset, adjust total duration from 0.2 to 3.0 seconds, then Apply. Short clips automatically limit their half of the transition. Remove restores a clean cut; Cancel leaves the edit unchanged. Apply and Remove are undoable.

The 20 presets are Black dip, White flash, Crash zoom, Whip left, Whip right, Whip up, Whip down, Spin clockwise, Spin counter, Zoom pull, Tilt punch, Bounce, Swing, Squeeze X, Squeeze Y, Iris, Wipe left, Wipe right, Shutter, and Warm flash. None is a separate clean-cut option, not counted among the 20.

Thumbnail, preview and export share deterministic easing and transformation calculations. Iris/wipe/shutter geometry also renders in GPU export. Transitions preserve clip timing and audio placement. They are cut-centered outgoing/incoming effects: this collection does not introduce overlapping cross-dissolves, optical-flow morphing, motion blur or audio crossfades. Presets do not require network access or AI.


## 0.17 creator grading and transition update

- Twenty animated cut transitions are available from the junction icon between adjacent clips: Black dip, White flash, Crash zoom, four directional whips, two spins, Zoom pull, Tilt punch, Bounce, Swing, Squeeze X/Y, Iris, Wipe left/right, Shutter and Warm flash. The gallery shows motion previews, descriptive notes and an adjustable 0.2–3.0-second duration. The actual applied duration is bounded by adjacent clip lengths. Changes are undoable.
- Transition transforms, color pulses and iris/wipe/shutter masks are calculated at each clip's local time. Preview and Media3 export use the same transition function. Clips retain their original timing and sound.
- Adjust includes draggable five-point Master, Red, Green and Blue tone curves, curve strength, S-curve, lifted-shadow and soft-highlight presets. Curves save with each clip. The live preview responds using a hardware color-matrix approximation of the curve endpoints; exported video applies the full five-point curve. This keeps adjustments responsive on TextureView devices, though the preview does not reproduce every curve bend exactly.
- Looks now includes Natural, Cinema, Vlog, Warm, Cool, Fade, Golden Hour, Teal & Orange, Moody, Pastel, Bleach and Vintage. Each has an adjustable intensity and animated swatch preview. Camera recording uses the Camera2 tone curve when the device reports support; looks remain unavailable on unsupported cameras.
- Text's font panel can import a user-supplied TTF/OTF into private app storage. Fonts are validated before use and referenced by drafts. Six bundled OFL fonts remain available offline.

The requested “Bo Sanchory” name did not resolve to a verifiable public font download/license in web searches. Use Import TTF/OTF with the exact file when you have it; the app does not bundle an unverified font. The current transition effects are cut-centered, without overlapping cross-dissolve, optical flow or motion blur. CapCut's official materials describe curves, color grading, filters, crop, keyframes and speed curves as editing features; this update implements curves and additional looks, not full feature parity. Further candidates include HSL controls, color wheels, adjustment layers, speed curves and audio fades. [CapCut color grading](https://www.capcut.com/resource/capcut-color-grading) · [CapCut editing features](https://www.capcut.com/resource/how-to-use-capcut).


## 0.18 creator editing controls

- Canvas offers Original, 9:16, 16:9, 1:1 and 4:3. The preview expand control opens playback full screen and returns to the current edit position.
- Transform has a clip opacity slider. It multiplies with opacity keyframes, so static opacity and animated alpha can be combined.
- Freeze adds the frame under the playhead as a two-second, full-canvas still layer. It can be moved, trimmed, transformed and extended like other timeline objects; source audio continues underneath.
- Text has 40 reveal presets, including typewriter, wipes, iris, pop, wave, sparkle, confetti, fireflies, stardust, bubbles, snow, hearts, stars and particle sweeps. The selected reveal renders in live preview and export.
- Voice enhancement is an optional offline export filter for video and audio layers. It applies a gentle low-frequency rumble filter, low-level expander and soft compression. It is not AI noise removal; preview playback remains unprocessed.
- Animate adds simple in/out fade and zoom-combo toggles alongside the existing still/zoom/pan/fade controls. Curve handles and keyframe easing remain available for motion and opacity.

Reverse playback and AI subject background removal are not included in this build. Reverse requires decoding and re-encoding frames; background removal needs a segmentation model. Geometric masks remain available, but they do not isolate a person or object.


## 0.19 mobile editor workspace

The editing screen uses familiar mobile NLE navigation: project title and undo/redo/export in the top bar, preview and transport controls above a multi-track timeline, followed by a single-row hierarchical toolbar. The first level contains Edit, Audio, Text, Overlay, Effects and Canvas. Tapping a category replaces that row with its tools and a back breadcrumb in the same row; tapping the breadcrumb returns to categories. No second toolbar row is stacked. The timeline remains directly below playback, with a fixed playhead and independently selectable tracks.


## 0.20 compact tools and capacity view

- Replaced the two stacked editor tool strips with one horizontal toolbar. The root row shows tool categories; choosing a category replaces it with that category's tools and a breadcrumb/back control. The back control returns to the root row.
- Added a Storage & model packs screen in the project library. It measures APK/split payload, editor draft/freeze storage, app cache, optional model directory, available device storage, total/available system RAM and Android's per-app heap class.
- Optional model entries are estimates from the product plan and are clearly marked not installed. No model files, AI features or downloader are included in the current build.
