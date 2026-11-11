# Screen-capture and OBS workflows, checked against Android 0.52.1.
add('Screen recording','Switch cameras and demonstrate monitor pairing',None,r'''
H|Front to back without stopping screen capture

Enable **Show presenter camera - front / back** before starting. During capture, **hold the bubble** to switch lenses. A short tap still expands or minimizes it. The Screen recording page also has **Switch to back/front presenter camera**, and the notification offers **Front / back**.

The change stays in the same screen-recording file. A short camera-reopening pause can occur. Mirroring applies to the front presenter; the back presenter is not mirrored by that setting.

H|Demonstrate Camera or Monitor while capturing your screen

1. Start the screen recording, then return to Home. Open the Camera workspace on the phone you want to demonstrate.

2. On phones that cannot open both cameras at once, the presenter temporarily hides and releases its camera. Screen capture continues. The Camera workspace or QR scanner takes priority.

3. Pair the monitor through the normal camera-pairing workflow. Leave the camera phone's Camera workspace visible while using the monitor.

4. Leave that workspace when finished. The presenter can resume once the hardware is released. Check its status on the Screen recording page.

N|Camera-pairing codes are protected from capture. The recording may hide that dialog; scan the physical phone display. The screen-demo connection to Windows uses a separate connection from camera-to-monitor pairing. Full two-phone pairing during a screen recording still needs an isolated end-to-end check. This does not enable front/back switching during an ordinary camera-video take.
''')

add('Screen recording','Point, draw and highlight your demonstration','45-annotation-tools',r'''
H|Enable floating tools

Before starting, enable **Pointer, pen & highlighter**. If asked, allow **Display over other apps** for Solo Camera, return to the app and start again. Approve entire-screen capture so the floating tools can be recorded.

H|Choose a tool

**Touch (hand):** operate the app underneath. Existing marks remain visible.

**Pointer (rings):** touch and move to show a marker under your finger. It disappears when you lift your finger.

**Pen (pencil):** draw arrows, circles or handwriting with your finger.

**Highlighter (bar):** draw translucent strokes so underlying content remains visible.

Use the color dot to cycle through five colors. Use the width control to cycle through three sizes. The selected tool is highlighted.

N|Pointer and drawing modes consume your touches. Choose the hand before tapping a button, scrolling an app or moving the presenter. Writing is freehand ink, not automatic handwriting-to-text conversion.
''','Floating annotation toolbar captured in an earlier build; these controls remain available in 0.52.1. The toolbar itself appears in the recording.')

add('Screen recording','Manage marks and keep your demo usable',None,r'''
H|Undo, clear and move the tools

**Undo (curved arrow)** removes the most recent completed stroke. Tap repeatedly to remove earlier strokes. There is no separate annotation Redo control.

**Clear (erase symbol)** removes all current marks. It does not erase video already captured earlier in the recording. To correct that earlier section, edit the saved video afterwards.

Drag the dotted grip to move the toolbar vertically along the right edge. Use the double-arrow control to collapse or expand it. On a short display, scroll inside the toolbar to reach its lower tools.

H|A reliable demonstration rhythm

1. Select Touch and navigate to the screen you want to explain.

2. Select Pointer, Pen or Highlighter and explain one point. Choose a color with good contrast against the background.

3. Clear marks that would obscure the next screen, then select Touch to continue navigating. Collapse the toolbar if you need more room.

4. Use Stop & save at the end. The floating tools and presenter are removed when the session finishes.

H|Rotation and practical limits

Rotating the display clears the current annotations and restores Touch mode to avoid misplaced marks. Set the desired orientation before starting whenever possible. Ink is bounded to 200 strokes with a per-stroke point limit; it is designed for short explanations rather than a large drawing document.

N|If controls seem unresponsive, select Touch first. Protected apps or system screens can hide overlays. Keep passwords, messages and payment details out of the demonstration.
''')

add('Screen recording','Send a live phone demonstration to OBS','48-live-obs-settings',r'''
H|Prepare the phone and receiver

Use **Android 0.52.1 and the updated Windows receiver**. Keep the receiver's complete folder together. Put both devices on the same Wi-Fi or connect Windows to the phone hotspot. Internet is not required for the local connection.

1. In a Windows receiver slot, click **Pair by QR**. Choose the shared-network address if more than one appears.

2. On the phone open **Screen recording** and enable **Live screen demo to OBS**. Choose microphone narration and/or **Include app audio in live demo**.

3. Enable the presenter and drawing tools if needed. Start recording and approve Android's entire-screen request.

4. Scroll to **Pair Windows - scan QR**. Scan the receiver code and confirm **Connect** for the intended computer. Reopen an expired Windows code if needed.

5. Wait for live video in the receiver slot. The phone's status shows the Windows connection state. Return to your demonstration.

N|Live mode also saves a local phone recording; there is currently no stream-only switch. Protect private screen content. The receiver does not start a public broadcast automatically. The screen/audio path was verified on TECNO and OBS in the earlier receiver test; the pictured 0.52.1 controls were captured from the connected TECNO.
''','Current 0.52.1 Screen recording page, showing the OBS, app-audio and narration switches before recording starts. Pair Windows appears after recording begins.')

add('Screen recording','Choose the live feed OBS should receive','49-screen-recording-controls',r'''
**Webcam** on Home sends the phone camera picture to the Windows receiver. Choose it when the phone is acting as a camera for a presenter or an event.

**Screen recording** sends the phone display. Turn on **Live screen demo to OBS** before tapping **Start screen recording**. Choose microphone narration, allowed app audio, presenter bubble and drawing tools before Android asks to capture the screen. Once capture starts, use **Pair Windows - scan QR** to connect to the receiver. The phone also saves a local MP4.

In the Windows receiver, select the connected phone's slot, set up or reconnect its OBS scene and confirm both moving picture and an active audio meter. Starting the phone capture alone does not start an OBS recording or public stream; press Record or Start Streaming in OBS when ready. End each separately.

N|If you need camera video and a phone-app demonstration in the same OBS production, use separate phones or slots and switch OBS scenes. One phone's Webcam and Screen recording modes are distinct capture workflows.
''','Current 0.52.1 controls after scrolling. Start screen recording is available; Stop & save becomes available during a recording.')

add('Screen recording','Route screen-demo video and audio through OBS',None,r'''
H|Connect OBS and select your feed

1. In OBS enable **Tools > WebSocket Server Settings > Enable WebSocket server**, keeping authentication enabled.

2. In the Windows receiver enter the local OBS port/password and choose **Connect OBS**.

3. Choose **Set up webcam scenes** for initial setup, or **Reconnect existing OBS feeds** after restarting the receiver. Set up before OBS starts recording or streaming.

4. Select the phone's slot and the appropriate Solo Camera scene in OBS. Check its picture and audio meter before going live.

5. Start your broadcast or recording using OBS's own controls. Stop & save on the phone ends the phone feed and finalizes the phone copy; manage OBS's output separately.

H|If you add the Browser source yourself

Copy the receiver's clean-feed URL. In OBS add a Browser source, paste it, set its dimensions to your OBS canvas and enable **Control audio via OBS**. The URL changes when the receiver restarts. Use the current address and keep the receiver running.

H|Choose which phone supplies audio

Single-slot output uses that slot's audio. The four-slot grid uses audio from slot 1. Check mute and volume in the OBS mixer. Avoid two copies of the same phone feed in one scene, which can double the sound.

N|OBS Virtual Camera exposes video only. Phone screen-demo audio is routed into OBS for recording/streaming; Zoom or a browser call still needs a separate audio-input arrangement. Ordinary camera-webcam mode remains a video feed.
''')

add('Screen recording','Choose audio and use a USB feed',None,r'''
H|What the audio switches do

**Record microphone narration** captures your explanation. **Include app audio in live demo** captures permitted media/game audio from the demonstrated app. Enable both to request a mix. Android's audio permission is needed even when capturing app playback without narration.

Apps can prohibit playback capture. Protected content, calls and arbitrary system sounds are not guaranteed to be captured. First make a short test of the actual app you will demonstrate. Avoid speaker feedback by using headphones where practical.

Selected live audio travels as 48 kHz mono PCM to OBS and is saved as AAC in the phone MP4. Give Stop & save time to finish audio finalization. If an audio error is displayed, check the saved file before relying on it.

H|Use USB after pairing

1. Connect one unlocked Android phone by USB. Authorize USB debugging on that phone.

2. In the Windows receiver enable **Use USB feed after QR pairing** before pairing the slot.

3. Pair by QR over Wi-Fi/hotspot as usual. Registration uses that local network; the subsequent media connection uses USB.

Windows needs Android platform-tools/adb in the user's Android SDK directory. If unavailable, use Wi-Fi/hotspot instead. This is a development-tool USB bridge, not automatic driverless USB support. Receiver-created USB forwards are removed when it closes normally.

N|Live preview uses a 720-pixel maximum long edge and up to 15 fps. Static screens may update less often. The phone copy keeps the selected capture dimensions. A short timestamp buffer helps align audio/video; zero delay and universal lip-sync are not guaranteed.
''')

add('Screen recording','Troubleshoot live demonstrations',None,r'''
H|The receiver is connected but OBS is black

Check that the receiver slot has video, the intended OBS scene/source is visible, and the feed URL is current. Reconnect existing OBS feeds after a receiver restart. Wait until a safe pause before changing sources if OBS is already recording or streaming.

H|Picture works but there is no sound

Confirm that the phone's narration or app-audio switch was enabled before starting. Allow Android audio permission. In OBS enable Control audio via OBS, unmute the Browser source and check the selected slot. Grid output takes slot 1 audio. If a particular app remains silent, it may block playback capture.

H|The feed is delayed or jerky

Compare the receiver preview and OBS. Use a strong local connection, close heavy exports or other video jobs, and try the USB feed if configured. The screen stream is capped at 15 fps; idle screens can send fewer changing frames. An OBS delay filter, extra scene processing or the final broadcast platform can add delay.

H|The presenter vanishes while showing Camera or scanning

The Camera workspace or QR scanner takes camera priority on phones that cannot open both. Screen capture continues. Return from that workspace to let the presenter resume. A presenter error is shown separately and does not intentionally terminate the screen recording.

H|What has been checked

On TECNO, real screen capture and a generated app-audio tone reached the Windows receiver and OBS's audio meter, while a playable MP4 with audio was saved. Microphone-plus-app mixing, screen-specific QR pairing, extended heat/battery behavior and different USB microphones still need separate isolated coverage. Test your real demonstration setup before a public session.
''')
