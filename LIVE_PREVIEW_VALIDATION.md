# Live preview validation — Android 0.39.0 / Windows live receiver

Implemented:
- Negotiated packet type 4 carries a monotonically increasing frame ID. Compatible old clients remain on type 2.
- At most four acknowledged frames in flight, plus one replaceable pending image. Delayed receivers skip old pending images; stale acknowledgments cannot release a different frame. A missing acknowledgment closes the link after three seconds.
- Header and encrypted payload use one socket write rather than separate tiny header/body writes.
- Windows receive/encryption and local OBS HTTP serving run outside the WPF synchronization context. Native preview refresh targets 30 fps.
- OBS HTTP frame requests wait up to 250 ms for a changed sequence, avoiding repeated duplicate JPEG requests; this wait returns as soon as a new frame arrives.
- Live priority chooses a supported lower-resolution preview when available and JPEG quality 55; Detail priority uses larger previews and quality 70. Recording resolution/FPS are preserved.
- Existing OBS source URLs can be reconnected after restarting the receiver. No OBS stream/recording is started or stopped.

Validation:
- Slow-receiver encrypted socket test passed: no more than four frames in flight, 95 old pending images skipped, stale ACK rejected and control messages delivered while video is blocked.
- Authenticated transport roundtrip, wrong key, tamper, replay, oversized packet and key-generation tests passed.
- Windows self-tests and APK / desktop builds passed.
- Real TECNO → Windows negotiated live connection over Wi-Fi passed, including a local OBS HTTP request that waited for a newer frame and disconnect clearing. The final short run delivered 99 frames in five seconds (19.8 fps). This is throughput, not latency.
- OBS diagnostics reported 30 fps, approximately 7.1 ms average frame rendering, and no delay filters on Solo Camera Browser sources. These are a snapshot, not a latency benchmark.
- The previously running Windows receiver still contained the old polling delay and was replaced with this build.

Remaining validation:
Actual glass-to-glass delay in milliseconds and perceived delay in a Zoom call have not been measured. Camera exposure/readout, JPEG encoding, Wi-Fi transmission, rendering and Zoom each have nonzero delay. Do not describe this build as instantaneous or infer end-to-end latency from frame rate. A user visual check and a clock-in-view measurement are the next validation steps.
