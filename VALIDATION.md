# Validation · Solo Camera 0.2.0 developer preview

## Automated checks

- Debug APK compilation: Gradle `:app:assembleDebug` passed on the final source. APK timestamp was verified newer than every app source/resource file.
- Android compatibility/static analysis: `:app:lintDebug` passed with **0 errors and 22 warnings**. Remaining warnings concern the non-latest target SDK, conservative usable-space checks, touch accessibility, English UI strings, and older drawing API suggestions. They are not treated as evidence of production readiness.
- Pure Java transport suite: six real-loopback-socket tests passed. Checks cover bidirectional ordered delivery, wrong keys, length bounds, ciphertext tampering, packet replay, and generated key encoding/uniqueness.
- Pure Java slider suite: seven checks passed, including logarithmic ISO endpoints/midpoint, 1,001-step zoom round trips, focus monotonicity, signed EV, clamping invalid metadata, fixed ranges, shutter exposure limits, and invalid configuration rejection.
- APK signature: Android SDK `apksigner verify --verbose` passed (APK v2 signature, one signer).
- Package remains `studio.solocamera`, minimum API 29 and target API 35. Version 0.2.0 increments the version code to 2. Permissions remain Camera, Record Audio, Internet, and Access Network State.

Tools: cached Gradle 8.10.2, Android Gradle Plugin 8.8.2, JDK 17, SDK Platform 35. Transport and slider tests validate their components, not the complete app or an independent security audit.

## Physical-phone UI check

An initial 0.2.0 build installed successfully on a Realme RMX3085 running Android 13 (API 33), with existing camera/microphone grants preserved. Launch succeeded. A device screenshot was inspected showing the live rear-camera preview, on-screen shutter dock, slider, Auto/Manual modes, presets, all eight control tiles, fixed f/1.8 aperture, and the new record control. Manual ISO and shutter readouts were visible together with live preview.

That visual check identified small toolbar icons on the high-density display. The final source corrects intrinsic icon dimensions to density-independent sizing and avoids redundant HUD text/background updates. The final APK compiles, passes lint, and verifies its signature. The phone disconnected before its final reinstall and before a controlled landscape/slider sweep could be completed; these checks remain pending. No audio/video recording, saved-clip playback, sustained capture, or real two-phone control session has been verified.

## Required phone tests before release

1. Install the same APK on two Android 10+ phones. Try denied camera and microphone permissions. Confirm Monitor mode works without granting capture permissions.
2. Pair on home Wi-Fi and a hotspot. Verify wrong-key rejection, camera-side approval, Deny, monitor revocation, and new-key rotation. Verify a previous key cannot connect to a newly created session. Check live slider adjustment from the monitor while recording.
3. Check camera preview and aspect ratio against saved video. Rotate each phone separately; confirm orientation metadata in the final recording. Try front and each exposed rear lens.
4. Exercise each enabled control using its slider and presets without opening the keyboard. Compare the image and capture metadata. Confirm the thumb is not reset by incoming metadata during a gesture, the final release value is applied, and auto/manual mode changes update correctly. Verify fixed aperture cannot be changed, unsupported controls are disabled/rejected, and lenses/stabilization cannot change during recording.
5. Record at least ten short takes. Validate that Start/Stop responses match the camera's actual state and every saved clip plays with sound, correct duration, and synchronized audio/video. Check that stop failures never display “Saved.”
6. Record a 30-minute take with monitoring. Measure actual frame rate, dropped frames, audio/video drift, latency, battery, storage, and thermal behavior. Device support cannot be declared from this build alone.
7. Disconnect the monitor for 60 seconds while recording. Confirm the camera continues recording while its app is visible, stale status disables monitor controls, and manual reconnection retrieves actual recording state before use.
8. Background or lock the camera phone during a take. Confirm it stops and finalizes the recording under this foreground-only policy. Test exiting camera mode and returning.
9. Test low storage, camera interruption by another app, and recorder failure. After an interrupted take, try recovery; it may publish only files with readable metadata and must not claim to repair unfinished MP4 headers.
10. Create and reopen multiple scripts, including non-Latin text and a long script within the 50,000-character cap. Test speed, font size, manual scrolling, play/pause, restart, paragraph navigation, both layouts, and text mirroring.
11. Enable recording-linked scrolling. Confirm it waits for recording confirmation, script pause leaves recording running, Stop pauses linked scrolling, and a canceled countdown never starts a take.
12. Test optional camera-side prompting with permission enabled on both phones. Verify disabling that permission prevents subsequent remote script updates. Test disconnect while scrolling and manual resynchronization after reconnect. Continuous exact cross-screen reading alignment is not promised in this build.
13. Inspect recorded clips to confirm script text and UI overlays never appear in footage.
14. Review small-screen, landscape, font-scaling, touch-target, TalkBack, and keyboard behavior on actual phones before wider distribution.

## Release gate

Do not mark this as a stable public release until the device tests pass on a published compatibility matrix, the UI is visually verified, failure/recovery behavior is confirmed, and the custom pairing/transport protocol is reviewed. QR pairing, low-latency streaming, and the remaining roadmap features are separate work.


## v0.3.0 QR pairing
Build and Android lint passed. All 15 transport, slider, and QR checks passed, including real QR bitmap encode/decode and malformed payload rejection. Scanner camera preview and a physical two-phone QR connection remain unverified. Installed and launched v0.3.0 successfully on the Realme RMX3085 using ADB push installation.


## v0.4.0 hotspot routing
All 16 automated checks passed, including hotspot subnet selection with non-octet masks and invalid inputs. Build and lint passed. Installed on Realme RMX3085. A desktop test client authenticated over the actual local Wi-Fi LAN to the Realme, received camera approval, and decoded 30 live JPEG frames plus 27 state packets in six seconds. Images were decoded in memory, not saved; no recording was started. This verifies the real CameraEngine → PeerLink → SecureChannel path over LAN. A physical two-Android-phone hotspot/QR test and Android monitor Wi-Fi binding behavior remain unverified because only the Realme was connected. Do not interpret the LAN probe as a completed hotspot test.


## v0.5.0 white balance fine adjustment
All 18 automated checks passed, including exact preservation at zero, 2,001 monotonic adjustment steps, warm/cool direction, bounded gains, and rejection of invalid values. No Android device was connected during this change; physical color response, hardware capability availability on the TECNO/Realme, and two-phone control remain unverified. Relative warmth is deliberately not presented as calibrated Kelvin.

Android build and lint passed for v0.5.0. APK freshness against all app source/resource files verified.


## v0.6.0 camera flip and video quality
20 automated checks pass, including bitrate scaling, encoder bounds, exact resolution naming and invalid format rejection. Android build/lint passed. Installed on TECNO KM7k. Physical UI confirmed Rear 0 → Front → Rear 0 switching; default 1080p30 and selected 2560 x 1440 / 30 preview worked. A test recording was started and stopped; MediaStore metadata confirmed 2560 x 1440, duration 31,424 ms and size 121,635,381 bytes (roughly 31 Mbps including container/audio). The test clip remains in Movies/SoloCamera. Higher resolutions, 60 fps, monitor-controlled flip/format changes, sustained/thermal recording and Realme formats remain unverified. The phone intermittently reported a thermal warning during these checks.


## v0.7.0 preview performance, quality labels and USB input
Android build and lint passed. Automated coverage includes the existing 20 checks and an encrypted PeerLink preview wake-up check: 50 of 50 generated frames delivered at 50 fps over desktop sockets. Installed and launched on TECNO KM7k. The real camera LAN probe decoded 96 JPEG frames and 23 state packets in six seconds (about 16 fps); the camera status reported 15 fps locally. This is below the 30 fps target and does not establish sustained recording performance. No camera frames were saved by the probe.

Preview encoding runs on a dedicated worker, monitor decoding is bounded and off the socket/UI threads, and outgoing preview no longer waits on the control queue timeout. Recording resolution is separate from reduced monitoring resolution. Resolution labels include 1080p, 1440p / 2.5K, 3K and 4K only where formats are supported.

USB microphone selection and recorder routing checks compile and pass lint, but physical USB receiver routing, lip-sync, constant receiver latency, long-take drift, two-Android-phone monitoring and hotspot performance remain unverified. No claim of measured audio synchronization correction is made. A clap test with the actual receiver at the start and end of a take remains required.


## v0.8.0 frame-rate slider, creator looks and voice processing
23 automated checks pass: the prior 21 plus recording-rate policy checks and creator curve checks. Coverage includes every integer from 10 to 30 in a manual-capable timing fixture, rejecting unsupported automatic rates and impossible frame durations, monotonic bounded RGB curves across all looks and multiple intensities, a common neutral baseline, warm tint direction and invalid intensity rejection.

The live HUD exposes FPS and LOOK tiles with sliders and preset chips. FPS availability intersects regular camera timing, current preview timing, hardware AVC size/rate support, automatic FPS ranges and manual sensor timing. FPS changes are blocked during recording, clamp shutter to the frame period and update the requested recorder bitrate. Custom timing switches to manual exposure; Auto is blocked until an automatic FPS preset is chosen. Camera sensor timestamp cadence is reported separately from rendered preview FPS. Color curves are submitted in the repeating camera request used by both preview and the recording surface; unsupported cameras retain Natural.

No Android device was connected during this update. Physical slider usability, actual 10/24/25 fps clip cadence and A/V sync, saved color appearance, two-phone remote adjustments, front/back capability differences, USB receiver processing and long-take stability remain unverified. Voice processing requests Android VOICE_COMMUNICATION source before recorder preparation. There is no integrated AI cleanup service, software microphone gain slider or guarantee of background noise removal. Physical receiver gain remains external. Final Android build and lint passed. APK freshness verified against all app source and resource files. Installation remains pending because no phone is connected.


## v0.9.0 separate playback and editing workspace
25 automated checks pass. The new editor timing checks cover source-to-project time mapping at speeds from 0.25x to 4x, more than 2,400 mapped positions, trim boundaries, split margins and invalid timing. Caption tests cover wrapping, explicit line breaks and emoji preservation. Existing camera, QR, white balance, format and encrypted transport checks remain passing.

Added a separate EditorActivity, private atomic project saves, session undo/redo, app-owned recordings/export library, Android document imports, player controls and an editable sequential timeline. Media3 1.8.0 provides matching preview/export compositions, linked speed effects, gain processing, creator looks, color adjustment, captions, rotation/mirroring, canvas fit/crop and looping music. H.264/AAC exports use a private temporary file and pending MediaStore publication, with source preservation and lifecycle cancellation. No broad media read permission or AI service was added. Portrait and landscape editor layouts compile. Final Android build and lint passed. APK version and freshness verified against app source/resources.

The TECNO was connected initially but disconnected during implementation. No physical playback, project restore, file-picker import, multi-clip preview, audio mixing, speed-adjusted A/V sync, caption placement, saved export, sharing, cancellation or landscape visual test was completed. These features must not be represented as device-validated. The build uses an experimental CompositionPlayer API and OEM compatibility remains unverified. No AI model, face smoothing, voice filters, automatic captions, transitions or keyframes are implemented.

Next device validation: open a saved take and seek/play/pause; create a short trimmed project; split/reorder/duplicate and undo; compare selected looks/caption/canvas in preview and a 720p MP4; verify audio and duration at 0.5x/2x with music; reopen the draft; cancel an export; switch back to shooting and verify the camera can reopen. Confirm original recordings remain unchanged.


## v0.10.0 multi-track timeline, independent edits and framing
26 host checks pass, including track-local video/PIP/caption/audio trim, preserving overlay tails after a main-video trim, round-tripping mask/animation/voice/frame settings and neutral defaults for older drafts. Android debug build, Android test build and lint pass. The signed APK verifies with APK Signature Scheme v2. Installed versionCode 10 / versionName 0.10.0 on TECNO KM7k.

The custom timeline has a time ruler, filmstrip thumbnails, a fixed moving-relative-to-content playhead, drag scrub, pinch/button zoom and distinct main-video, text, PIP and audio selections. Existing per-clip captions migrate to independent project-time text layers. Toolbar trim keeps the selected track and does not rewrite other tracks. Captions can be independently split, duplicated, timed and positioned. Up to three muted video overlay tracks and thirty text layers are supported. The project duration includes overlay/text/explicit audio tails; uncovered video tail areas render black.

On-device instrumentation exercised the actual Text → Trim and PIP → Trim toolbar routes and their buttons, verified main-video trim leaves caption/PIP timing intact, exercised multi-touch pinch resizing through the preview's gesture handler, and exported a masked/feathered, cropped and animated PIP plus timed text. A 3-second export was published with 3,051 ms metadata; a second project whose main video ended at 600 ms retained its PIP/text tails and exported 2,581 ms for a 2,500 ms project (codec/container padding). MP4 frame inspection confirmed full color, the feathered elliptical PIP, lower-layer visibility, caption placement, caption disappearance after its end, and PIP/text continuing on black after the main video ended. Both source and exported MP4s were decoded for inspection; the source was not modified. Test media/frames and private device data remain in excluded qa/ and are not bundled with source delivery.

An initial experiment with a leading blank video gap failed in CompositionPlayer. Overlay sequences now use hidden real-video preroll and loop until the non-looping main timeline ends, with alpha controlled by project-time visibility. Export and preview share the same composition and GPU effects. The new centered masks are ellipse, rectangle and linear with adjustable feather. Animation presets provide zoom in, zoom out and pan; there is no keyframe editor, arbitrary/freehand mask, tracking, subject segmentation, AI denoising or full CapCut parity. Imported music is one independently timed/repeated source, not a full multi-clip audio workstation. Voice pitch uses Sonic processing and needs listening/sync checks across more sources/devices.

Portrait timeline/library UI was visually inspected earlier in this update. During the final device checks WhatsApp held a proximity screen-off wakelock, so the gesture regression used synthetic multi-touch input with explicit view bounds; final hands-on pinch ergonomics and the final landscape layout remain unverified. Sustained playback, three simultaneous PIPs, very large projects, thermal behavior and Realme compatibility require further physical testing.


## v0.11.0 overlay crash and editable objects
Android debug/test build and lint pass (0 lint errors). All 26 host checks pass, including new independent stretch-axis serialization. Installed version 0.11.0/code 11 on TECNO KM7k.

Reproduced heap exhaustion with the user's two 1440x2560 overlay segments. CompositionPlayer's multi-input preview also advanced its clock while displaying a frozen frame, so clock-only checks were insufficient. Preview uses an asynchronously rendered 360x640 cache and standard ExoPlayer for overlay projects. Both CompositionPlayer and Transformer ExoPlayerAssetLoader now have bounded 4 MiB/2.5-second input buffers. The latter's original 50-second budget caused additional heap exhaustion in long renders. Muted overlay inputs do not generate audio. The main-video tail uses black decoded frames rather than generated video gaps.

Device regression passed for the cloned 47.1-second real project: complete cached preview, rendered first frame, playback, seek to 6.5 seconds and advancement beyond 9 seconds across the overlay split/main-video end. Java heap measured about 30 MiB during the final render. Decoded cached MP4 duration 47.007 seconds, 360x640; frames at 0.5/2.5/7/12 seconds visually confirm changing source frames, PIP visibility, and overlay continuation on black. User project files were not modified.

Actual text-row touch selects text; synthetic pinch modifies text object zoom. Per-track toolbar trims remain independent. A stretched/cropped/feathered text and animated/masked/stretched PIP project exported successfully with independent tails. Selected rows have edge trim handles. Text layers have individual rows. Preview objects have a bottom-right width/height stretch handle, continuous pinch zoom and offset-preserving drag. Text and video support crop, centered masks, feathering and preset animations. Portrait UI visually inspected. Three-overlay stress, long recording projects, thermal behavior, precise audio sync/listening, landscape ergonomics and other phones remain unverified. Overlay cache regeneration introduces preparation latency after edits; this update does not provide instantaneous multi-track GPU preview or unlimited tracks.


## v0.12.0 live editor playback
Supersedes the cached preview implementation in v0.11. On the connected TECNO KM7k, the cloned 47.1-second project produced its first preview frame in 750 ms. Instrumentation verified changing rendered frames, three end/replay cycles, responsive tool selection, transform changes without replacing the preview or its decoders, and adding a third overlay track without rebuilding the existing tracks. The third track overlaps the main video and one other overlay; this does not certify three simultaneous overlays plus a main video.

Screenshots confirmed visible base video and PIP, then immediate resized/stretched/feathered PIP geometry. A layout bug that measured the preview canvas at 1x1 and timeline at zero height was corrected. Inactive video decoders are released: retaining them reproduced a buffer stall after adding a track. Track-specific toolbar trimming, synthetic text/video pinch, and masked/animated export with independent overlay/text tails passed on the phone (QA export 32403). User source media and existing project drafts were not changed.

Preview now uses TextureView layers with a separate project clock; export remains the Media3 GPU composition. Color and high-gain audio preview are approximate. This is a targeted stability fix, not a claim of full editor feature parity or certification across Android devices. Long-session thermal behavior, exact audio sync, all look/export comparisons and landscape ergonomics remain unverified.

Final code also resets decoders at end-of-project replay and honors repeated explicit seeks to the same timestamp. Android build and lint pass; all 26 host checks pass. APK signature verification succeeds.

The device runner temporarily makes its test window non-touchable: physical timeline input was captured interrupting the automated playback assertions. Production windows remain touchable. Decoder play/pause is applied once after all visible layer readiness has been evaluated.

Final isolated device run: PASS first frame (729 ms), changing picture, three end/replay cycles, tool response, live transform without decoder replacement, third overlay addition, single-clip replay and playback across a main-video cut. The final build also passed track-isolated trim, text/video pinch and masked/animated export (QA export 32404). Version 0.12.0/code 12 is installed on TECNO KM7k.


## v0.13.0 deletion and timeline placement
Device crash logs confirmed IndexOutOfBoundsException in EditorLivePreview.render after overlay deletion: the live model had fewer layers than the preview's cached view list. Rendering now reconciles view counts against the current model, and committed edits bind synchronously. Selection is clamped after structural changes.

The first v0.13 build passed phone instrumentation on TECNO KM7k: deleting three overlays during playback, deleting a caption, deleting a selected main clip and undoing it, actual timed long-press/move/up input to place a main clip at 5000 ms, dragging it to a PIP row and back, timestamp serialization, and export with leading/intermediate gaps. Export 32405 was decoded: 0.5 s and 4.5 s were black; 2.5 s and 5.5 s contained video, matching the intended 0–2 s / 4–5 s gaps and 2–4 s / 5–7 s clips. Timeline drag UI was visually inspected.

The subsequent last-main-clip/empty-canvas extension and blank-row selection fixes build and pass lint; all 27 host check groups pass. Final device installation/regression is pending because the TECNO disconnected. The expanded moveOnly instrumentation covers last-clip deletion, Undo from an empty project, and overlay-only composition creation, but has not yet run against the final build. Earlier results must not be treated as validation of these later paths. Long-duration edge scrolling, all media codecs, caption/music dragging and other devices need additional hands-on validation.

Recovery checkpoint: reinstalled the packaged v0.13.0 APK on connected TECNO. Its final moveOnly device check passed overlay/caption deletion, main and last-clip deletion plus Undo, timestamp dragging, main/PIP transfers, persistence and gap export (QA output 32513). New dynamic-track/keyframe/text work in the workspace is incomplete and is not part of this installed APK.


## 0.14.0 verified on TECNO KM7k (2026-10-11)

Debug APK and test APK build successfully; lint has zero errors (47 warnings). Host suite passes, including dynamic eight-track serialization/collision checks and keyframe easing/style persistence.

Final installed build passed five video rows and five text rows, the Add/update keyframe button, multi-track playback, saved styles/keyframes, and animated styled-text/transition export (32514). Export frame and editor screenshot were visually inspected. Rows use staggered clips; five simultaneous decoders were not tested.

Deletion of three live overlays and text, main/last clip deletion and undo, long-press timestamp moves, main/PIP transfers, persistence and gap export passed (32515). Replay regression passed: first frame 624 ms, changing picture, three end/replay cycles, responsive tools, live resize without decoder recreation, overlay addition, single-clip replay and main-clip cut. Independent trim, text/video pinch, overlay/text tails and masked animated export passed (32516).

Initial short-overlay stress tests failed. The final preview reuses active overlay slots and retains paused decoders for up to 500 ms across short gaps; export groups sequential clips by occupied row. The stress test then passed. Large simultaneous compositions, all codecs and extended thermal/performance sessions remain unverified. README lists unfinished feature areas; this release does not provide full CapCut parity.


## 0.15.0 editor interaction update (2026-10-12)

On connected Realme RMX3085, the interaction runner passed background/shadow switches, live font selection, the preview keyframe button, automatic gesture-created second keyframe, labeled Undo/Redo, actual image decoding/import, a sticker layer, drag-created object row and MP4 export (1000123283). The first run caught a resize handle intercepting a center drag; the hit region was corrected and the next run passed. Exported image, sticker, serif text and fitted rounded background were visually inspected.

A subsequent preview regression attempt was blocked by the Realme lock screen: no initial preview frame was delivered while locked. This is not a passing playback regression. The final compact layout, version label and keyframed Rotate/Reset changes have host/build validation; their final device rerun remains pending unlock. Earlier v0.14 TECNO regression results are not final-device validation of v0.15.

Host suite passes text-toggle defaults/persistence, imported image path persistence, white/slide transition endpoints, keyframe curves, dynamic tracks, isolated trims and the previous camera/transport checks. Cut-centered white/zoom/slide transitions are implemented; a full overlapping cross-dissolve engine is not claimed. Images import as bounded stills; twelve built-in symbol/emoji stickers and custom PNG imports are available. AI effects are not included.


Final build and lint pass, with zero lint errors; all host tests pass. Final installation attempt on Realme failed with Android PackageInstaller IOException: Requested internal only, but not enough space. Device reports about 0.9 GB available. Installed package is the earlier interaction-tested code 15 build still labeled 0.14.0; the final 0.15.0 package includes the compact layout and keyframed rotation/reset. Do not report the final APK as installed. User was asked to free storage and unlock the device. The isolated test draft, image copy and export were removed; source fixture cache remains for the pending regressions.


### Final Realme verification completed, 2026-10-12

Final packaged 0.15.0/code 15 successfully installed after unlock. All four device check groups passed: text switches/live fonts/automatic gesture keys/visible undo-redo/image and sticker import/drag-created row/export (1000123286); isolated trim/pinch/masking/tails/export (1000123287); first frame 810 ms, three replay cycles, responsive transforms and overlay additions, single-clip replay and main cuts; deletion/last-clip undo/long-press timestamp moves/cross-track moves/persistence/gap export (1000123288).

The original seek test used a fixed 1.6-second delay, insufficient for the synthetic long-GOP source on this phone. It now waits for completion with an eight-second ceiling and then asserts exact end state and successful replay. The drag test now reads actual timeline scale and uses a zoom that avoids triggering edge auto-scroll on this screen. Production APK unchanged during these test adjustments. Landscape editor screenshot inspected. This supersedes the installation and lock-screen blockers recorded above; storage remains low but installation succeeded. Test exports, isolated QA draft and imported QA image removed after testing; user projects and recordings preserved.


## 0.16.0 final Realme validation (2026-10-12)

Installed and confirmed versionName 0.16.0/versionCode 16 on Realme RMX3085. Offline debug build, instrumentation build, Android lint (zero errors) and the complete host suite passed.

Device audio checks passed MP3, WAV and M4A imports through the activity import path; three independent rows; selected-item trim, split, removal and undo; playback and replay; all six bundled typefaces loaded without fallback; animated preset selection; mask/audio draft persistence; and mixed MP4 export. The first test assumed Reset trim also reset the timeline start; corrected the fixture to explicitly restore its start before checking a split. Reset trim intentionally preserves timeline placement.

Export audio FFT detected 440, 660 and 880 Hz at both 0.5 and 2.5 seconds, confirming all three sounds are mixed and the split audio continues. Visual review found the main video's mask alpha was ignored by opaque MP4 encoding. Corrected main masks to composite onto black and normalized shader mask coordinates to match preview. The rebuilt export (1000123300) was inspected and matched the offset, rotated and feathered preview mask with bundled Poppins text.

Final playback regression passed: first frame 732 ms, changing frames, three end/replay cycles, responsive tools, immediate transforms without recreating decoders, overlay addition, single-clip replay and main-clip cuts. Move/deletion regression passed live overlay/text removal, last-clip undo, long-press timestamp movement, transfers between main and PIP, gap persistence and export (1000123301). Interaction regression passed text background/shadow switches, font selection, automatic gesture keyframes, Undo/Redo, photo import, sticker layers, drag-created rows and export (1000123302).

QA exports and hash-matched imported QA assets are removed after inspection; the test APK is uninstalled and the main app reopened. Existing user projects and recordings are preserved. Third-party cloud providers, unidentified IT6 files, hundreds of simultaneous layers, extended thermal sessions and AI processing are not validated. AI is not included. Audio drag-edge handles are implemented; the automated audio test verifies toolbar trims, not a touch-driven edge-handle gesture. Preview amplification above unity and permanent off-timeline asset bins remain limitations documented in README.

## 0.17.0 creator editing update (2026-10-12)

- Offline Gradle build completed successfully for the debug APK, Android instrumentation APK and `lintDebug`; lint reports zero errors. The complete host suite passed, including all 20 transition IDs, bounded timing/endpoints and short clips; creator-curve bounds/monotonicity/intensity; saved curve persistence; looks; and existing editing, audio and transport checks.
- Installed and launched package `studio.solocamera` version 0.17.0/code 17 on the connected TECNO KM7k. A fresh screenshot confirms the app home screen opens. The device-side 20-transition playback/export regression passed. Exported clip-center frames were checked visually on a contact sheet: all 21 centers contain the source image, and samples around the cuts show the applied motion/mask/flash effects.
- The editor adds 20 animated transition cards with adjustable duration, a draggable Master/R/G/B curve panel, curve strength and curve presets, plus 12 adjustable creative looks. Transition timing and the GPU export path share the same transition calculations; transitions remain cut-centered and do not provide overlapping cross-dissolves or optical-flow interpolation.
- Added local TTF/OTF import and six bundled OFL font families. Web searches did not find a verifiable public download/license for “Bo Sanchory”; the app does not bundle an unverified copy. Users can import a licensed font file they already have.
- The curves export path has produced decoded nonblack frames in a prior device run. An earlier live-curve screenshot check falsely passed while the Realme was locked; the precondition now rejects a locked/asleep screen. The corrected test passed on unlocked TECNO KM7k: a strong four-channel curve/look change altered the visible preview pixels, persisted through reload and exported an MP4. The live preview uses a hardware color-matrix approximation of curve endpoints; export applies the full point curves.
- Feature comparison used CapCut's public material describing curve-based color grading and core editing controls: [color grading](https://www.capcut.com/resource/capcut-color-grading) and [editing features](https://www.capcut.com/resource/how-to-use-capcut). This update adds selected core controls and does not claim full CapCut parity. Further candidates include HSL/color wheels, speed curves, adjustment layers, audio fades, and expanded device-specific performance testing.

- The first TECNO transition export revealed that Media3 effect timestamps follow the composition clock. Clip/overlay timeline offsets are now subtracted before evaluating transitions, masks, keyframes, and clip fades. The rerun passed all 20 live effects and the full export; the 21 clip centers and transition samples were visually reviewed.

2026-10-14 — v0.32.2 on TECNO KM7k: gapExportOnly passed gap-preserving main deletion, automatic closure, unchanged text timing, persisted mode, toolbar control and MP4 export (media 33408). Existing full device regression passed masked animated export with independent overlay/text tails and audio (media 33409). Export now uses Media3 InAppMp4Muxer; failures log errorCode and full cause. Original user failure was not present in available logcat, so exact original project failure is not independently reproduced.

2026-10-14 — v0.32.3 export stall: reproduced non-progressing export on a copy of the creator's 14-clip, 179188 ms project with fractional speeds and screen recordings; encoder logged backward-timestamp frame drops. Replaced audio-callback-coupled experimental speed effect with constant SpeedChangeEffect plus SonicAudioProcessor speed processing. Same project exported to media 33412; Android metadata duration 179285 ms (97 ms difference), 1920x1080, both audio/video present, final frame decodes. Kept 10-second watchdog; did not increase timeout. Installed v0.32.3 on TECNO KM7k.

2026-10-14 — v0.33.0 TECNO device regression passed: playhead audio split preserves source cut/row; video audio detach renders trim and fractional speed, creates a separate track and mutes original only after success; row-specific selection excludes neighboring audio rows; select-all includes main/PIP/text/audio; M4A export has audio and no video; rendered compound combines all tracks, saves source project pointer and unpacks original layers. Compound is rendered with source unpacking, not a live nested editing workspace. Toolbar is horizontally scrollable on narrow phones. UI: long-press track label for track selection; Select for all/individual items and compound/unpack; scissors splits selected audio; Volume offers detach; Export dialog offers Export audio.

2026-10-14 — v0.33.1 compound aspect repair: existing creator portrait compound encoded as 1920x1080 with display rotation was incorrectly stored as landscape. Compound creation now reads rotation-aware display dimensions; opening previously created main/overlay compounds repairs stored dimensions without re-render or changing selected canvas aspect. TECNO tests passed actual existing compound fixture repair to 1080x1920 and new portrait compound canvas/dimension preservation, plus audio split/detach, selection, M4A export and unpack regression.

2026-10-14 — v0.34.0: audio-format selector supports WAV (16-bit PCM RIFF), MP3 (offline Jump3r/LAME core, 192 kbps at >=32 kHz) and M4A (AAC); WAV default. Conversion uses bounded MediaCodec decoded PCM buffers and cancellation; complete encoder source/license bundled in vendor and assets. Device test verified format sniffing, actual audio codec MIME, nonempty payload, WAV RIFF/WAVE signature, duration tolerance and no video track. Exports created: WAV 33510, MP3 33511, M4A 33512, MP4 33513. MP4 export publishes duration/dimensions/date_taken and scans the file after is_pending=0. Device inspection showed CapCut READ_MEDIA_VIDEO denied with READ_MEDIA_VISUAL_USER_SELECTED granted, explaining missing new exports in its picker; app now explains media access and offers Open/share. Adobe Podcast upload was not tested against a logged-in external account.

2026-10-14 — v0.35.0 on realme RMX3085: app-owned SpeechCleanup DSP replaces per-sample hard gate with envelope-based smooth expansion, 75 Hz rumble reduction, gentle high-frequency roll-off, compression and bounded output. Offline Gentle/Balanced/Strong settings and strength are persisted for video and audio. Same processor now runs in ExoPlayer preview audio sinks and Media3 exports. JVM tests passed quiet-background attenuation, voice retention, stereo isolation, DC reduction, bounded output and reset. Device regression passed PCM sample-count/timing, stereo/reset, enhanced playback, settings persistence and enhanced WAV export. This is signal processing, not a trained denoising model; quality against real-world noisy recordings has not been benchmarked.
