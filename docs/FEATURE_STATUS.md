# Feature status — Android 0.52.1

This document separates working local features from planned integrations. A visible option does not imply that an external service is live.

## Implemented in the app

- Android Camera2 shooting with capability-dependent controls, teleprompter, audio-route checks, frame guides and local recordings.
- QR-approved camera-to-monitor pairing on a local Wi-Fi network or phone hotspot, with remote controls and preview.
- Screen recording with presenter bubble/front-back switching, pointer and drawing tools, local MP4 save and optional live screen/audio feed to the Windows receiver.
- Studio projects with video, image, text, audio and FX tracks; preview, keyframes, transforms, masks, transitions, creator titles, audio tools, privacy blackout and video/audio export.
- A Windows WPF receiver that can create OBS Browser sources/scenes and use OBS Virtual Camera for a video webcam feed.
- A Publish Studio with local post drafts, channel choices, per-channel fields and a calendar that distinguishes drafts from confirmed service jobs.

## Requires external work or device-specific validation

- **Automatic social publishing:** no production publishing service URL, hosted worker, connected platform accounts or approved provider credentials are included. Facebook, LinkedIn, TikTok and YouTube connections show Coming soon until deployed and verified.
- **AI:** optional downloaded tools vary by feature; the app does not bundle a general-purpose model or promise AI output where no model/backend is implemented. Check the relevant UI before relying on an effect.
- **Hardware controls:** aperture is read-only on a fixed-aperture phone. Camera2 capabilities decide available lenses, ISO/shutter ranges, stabilization, frame rates and recording formats.
- **Live feeds:** network conditions, encoding and OBS processing add latency. Screen-demo live preview is capped at a smaller size and frame rate than the saved phone recording. Four simultaneous physical phones, every USB microphone and long-session thermal behavior need broader testing.
- **Export:** complex compositions may require substantial time, memory and storage. Verify final audio, picture, duration and privacy covers in the exported file.

See [VALIDATION.md](../VALIDATION.md) for specific checks and [the user manual](manual/README.md) for workflows.
