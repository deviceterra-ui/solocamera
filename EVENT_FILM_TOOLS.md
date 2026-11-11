# Event and short-film tools — 0.38.0

Installed on the TECNO KM7k, version code 42. Includes screen recording and the preview latency fixes.

Camera → Shooting setup (gear) → Event / film tools:
- Production/event and scene labels with an automatic next-take number. MP4 filenames include the labels, take number and timestamp. Slate changes are blocked during recording. Available from the monitor too; settings and filenames belong to the camera phone.
- Supported Off/50 Hz/60 Hz/Auto anti-banding controls for venue lighting. This requests the camera's auto-exposure anti-banding control; manual shutter and some LED drivers still require testing and adjustment.
- Optional confirmation before the local record button stops a take. It does not block emergency/system stops or remote stop commands from another client.
- Explicit exposure + white-balance lock/release actions when the camera supports them and automatic modes are active. Manual settings remain fixed already.

Device validation:
- Screen capture approved through Android's system picker. A 17-second MP4 saved at 720 × 1600, IS_PENDING=0, decoded with OpenCV. Microphone capture and rotation were not part of this test.
- Camera streaming over USB-forwarded encrypted TCP: 180 decoded JPEG frames plus state updates in six seconds (30 fps).
- Event tool controls rendered on the device; a camera take saved as JJ-QA_EventTest_T001_<timestamp>.mp4 at 1920 × 1080, about 21.5 seconds, IS_PENDING=0; next take advanced from 1 to 2.
- Filename unit checks passed: event/scene/take formatting, Unicode, invalid path characters and length bounds.

The low-latency Windows receiver remains a separate update. FPS measured here is preview delivery, not end-to-end OBS/Zoom latency. Anti-flicker effectiveness depends on venue lighting and the device. Future event work should prioritize audio meters/headroom warnings, thermal/storage diagnostics, and tested high-frame-rate workflows before adding heavy AI processing.

Additional live Wi-Fi check on the same phone: 149 decoded JPEG frames in six seconds (about 24.8 fps). This is a short preview-delivery measurement, not a guarantee of sustained performance or OBS latency. Temporary QA slate labels were cleared; saved test clips remain available for playback checks.
