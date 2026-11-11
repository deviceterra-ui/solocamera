# Architecture and source map

Solo Camera has three running surfaces: the **Android creator app**, an optional **Windows receiver**, and a **future publishing service**. The Android app contains all camera capture and editing code. The receiver is needed only when a computer should view a phone feed or pass it to OBS. The publishing service is specified in [PUBLISHING_ARCHITECTURE.md](../PUBLISHING_ARCHITECTURE.md) but is not implemented here.

## Capture and monitoring

`MainActivity` owns the foreground camera and monitor workspaces. `CameraEngine` serializes Camera2 mutations on a capture handler, manages preview/recorder surfaces and publishes state to UI listeners. `CameraControlsView` maps device capabilities to icon controls and sliders. `PeerLink`, `SecureChannel`, `PairingCode` and `NetworkAddress` handle local QR pairing, camera-side approval, encrypted messages and latest-frame transport. The monitor sends commands; the camera phone records and owns the saved file. Neither Wi-Fi pairing nor the hotspot workflow requires cloud infrastructure.

Camera capability metadata is authoritative. UI values such as aperture, frame rate, microphone route and resolution must not imply control that the chosen phone cannot provide. The preview is a monitoring stream, separate from the phone's recording encoder.

## Studio editor

`EditorActivity` owns the editing screen and tool dispatch. `EditorProject` serializes project instructions and source references; deleting or changing a timeline object should not mutate unrelated tracks. `EditorMotion`, `EditorEdits`, `EditorTransitions`, `EditorTitlePresets` and related classes compute timeline changes and title behavior. Media3 playback and Transformer export render the composition. Exported files are published through Android MediaStore; projects remain editable instructions, not copies of source footage. The selected source files must stay available until final export.

The privacy blackout is an opaque image-type timeline object. It renders above ordinary graphics and remains opaque even if opacity keys are present. Its time interval and position can be edited independently; source footage is unchanged.

## Screen capture and Windows

`ScreenRecordingActivity` collects user options and obtains Android's screen-capture consent. `ScreenRecordingService` is a foreground MediaProjection service that records the screen, manages the optional presenter/annotation overlays and finalizes a local MP4. In live mode, `ScreenLivePeer` sends bounded encrypted screen frames and selected audio to the Windows receiver. The receiver's `PhoneConnection`, `LocalFeedServer` and `ObsBridge` display the feed and manage OBS Browser sources through OBS WebSocket. A screen demo can carry microphone and permitted app audio into OBS. OBS Virtual Camera exposes video to other applications; audio must be routed separately there.

The receiver is not a social livestreaming service. The creator starts an OBS recording or broadcast separately. Pairing occurs on the local network; USB forwarding is an optional development-tool transport after QR registration.

## Publishing boundary

`PublishActivity`, `PublishDraftStore`, `PublishApiClient` and `PublishCredentialStore` implement the phone-side queue/calendar/accounts UI, local drafts, OAuth callback handling and the client contract for a future service. With no configured endpoint, account buttons remain unavailable and drafts never become confirmed scheduled posts. A production service must own provider tokens, durable jobs, media storage and provider workers. The four platform adapters and their approvals are separate from this Android repository.

## Dependency and license boundaries

Android media libraries and ZXing are pinned locally under `vendor/maven` and `app/libs` for reproducibility. The Windows receiver references the local QRCoder DLL. Bundled fonts, brand icons and codecs have individual licenses; see [THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md). No project-wide license is asserted here.
