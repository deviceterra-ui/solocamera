# Version-specific user-facing content. Loaded by build_user_manual.py.
add('Getting started','Your first finished video','01-home',r'''
H|A complete first session

1. Put the phone securely on a tripod. Open Solo Camera and choose **Camera**. Allow Camera and Microphone when Android asks.

2. Open **Shooting setup** using the sliders icon. Run **Pre-recording check**, then make a short microphone test. Choose the resolution and a supported frame rate.

3. Frame yourself. For a second-phone monitor, follow the pairing chapter before you start recording. A monitor is optional; one phone can record on its own.

4. Choose a countdown if you need time to get into position. Tap the round red Record button. Wait for the confirmed recording state and running timer.

5. Deliver a short take. Tap the timer to mark a good moment, or hold it to mark a mistake. Tap Stop and wait for the saved message.

6. Open **Studio**. Play the take first. Use **Edit this video**, or tap **New project** and add your assets.

7. Select a clip, move the white playhead and choose **Split**. Remove unwanted sections. Add text and sound as required.

8. Open **Export video**, choose 720p or 1080p and export MP4. Keep the app visible until saving finishes. Play the finished file and check picture and sound.

N|Recording, editing and exporting are separate actions. A saved project is an editable draft; it is not yet a finished video file.
''', 'Home is the starting point for Camera, Monitor, Webcam, Screen recording, Studio and publishing drafts.')

add('Getting started','Phones, permissions and first launch',None,r'''
H|What you need

Solo Camera requires **Android 10 or later**. Start with charged phones and enough free storage for source videos, temporary processing and final exports. Actual lens access, manual controls, stabilization, frame rates and maximum recording resolution depend on the phone and Android camera driver.

For remote monitoring, install the app on both Android phones. Connect them to the same Wi-Fi network or connect the monitor to the camera phone's hotspot. For Windows webcam use, also install the Windows receiver and OBS Studio; see the companion workflow later in this guide.

H|Grant only the permissions for your task

- **Camera:** needed for filming, a presenter bubble and scanning pairing QR codes.

- **Microphone:** required for camera video with sound, microphone tests and voice-over. Screen recording can be made without microphone narration.

- **Notifications:** allow the screen recorder to show its ongoing recording controls. Android wording varies by version.

- **Display over other apps:** needed for the presenter camera and floating pointer/drawing tools above other apps. Turn it on for Solo Camera in Android Settings, then return to the app.

- **Screen capture approval:** Android asks for a fresh approval each screen-recording session. Presenter, drawing-tool and live-demo modes request the entire display so the visible composition can be captured.

- **File access:** the Android file picker grants access to the files you select. Keep those files available while a project still needs them.

H|If you previously denied a permission

Open Android **Settings > Apps > Solo Camera > Permissions**. Enable the permission for the feature you want, return to Solo Camera and try again. Special overlay permission may be under a separate Special app access menu. Do not uninstall as your first troubleshooting step: local projects, scripts and model files can be lost.

N|This is a development build, not a guarantee of identical behavior on every Android device. Re-test camera and audio after a phone system update or a change of USB receiver.
''')

add('Getting started','Find your way around the app','01-home',r'''
H|Six destinations

**Camera** is the phone that captures and saves video. It provides the live camera controls and teleprompter.

**Monitor** is a second Android phone used to view and control the camera phone. It does not automatically receive the saved recording.

**Webcam** scans a Windows receiver QR code so the phone can become a local video source for OBS and compatible webcam applications.

**Screen recording** captures demonstrations with optional narration, front/back presenter and drawing tools, and can send screen demos plus selected audio to OBS.

**Studio** contains saved editing projects, app recordings, exports and media-import actions. Use the large **New project** bar to begin without choosing a video first.

**Schedule & publish** contains local post planning, a calendar and account placeholders. Automatic social publishing is not live in this build.

H|Common gestures

Tap a camera tile to open its live adjustment. Drag a slider and watch the preview. Tap the selected tile again or its close control to dismiss the adjustment.

In Studio, tap an object to select it. Hold a timeline object to move it. Pinch the timeline to zoom time; pinch the preview to resize the selected visual object.

N|Icons reveal actions for the current selection. If you cannot find a tool, select the correct object and use the menu's back arrow to return to its parent category.
''')

add('Getting started','Choose a sensible recording setup',None,r'''
H|A practical starting point

For a talking-head clip or product tutorial, start with a supported **1080p / 30 fps** format, automatic exposure and automatic white balance. Use a tripod and adequate light. Record a short test before moving into manual exposure. If preview performance is poor, select Live priority; this changes monitoring quality rather than the saved recording resolution.

For a deliberate film cadence, choose a supported 24 or 25 fps rate and test your shutter against the venue lighting. Higher frame rates need more light and may reduce the maximum available resolution. The slider offers only rates the app considers supported for the current format; it is not a promise of every integer rate on every camera.

For screen demonstrations, the smaller capture size usually uses less storage and processing. Use the presenter circle only when you need it. The screen recorder displays actual output dimensions rather than a camera-style 1080p label.

H|Avoid a failed take

Keep the camera app visible while filming. Leaving Camera mode, locking the camera phone or moving it to the background ends the active take and attempts to save it. Disconnecting only the monitor does not itself stop the camera phone's recording.

Do not change lenses, video format, stabilization or microphone routing mid-take. These changes are blocked during recording. Make them first, then verify the preview and run a sound check.

H|Keep source quality and preview quality separate

The monitor shows a compressed local preview; it is not the original recording file. Judge final sharpness, color and sound by playing the saved recording. Wi-Fi preview smoothness and OBS frame delivery do not establish the actual frame rate or quality of the saved camera video.

N|A slider changes only what the phone exposes. A fixed iris cannot become a physical adjustable aperture through software. Use light, ISO, shutter and exposure compensation appropriately instead.
''')

add('Camera','Read the camera workspace','02-camera',r'''
H|What is on screen

The top bar provides **Back**, **Playback and editing**, **Pair phones**, **Grid**, **Teleprompter** and **Shooting setup**. Hold an icon to see its tooltip where supported. Holding Grid opens the framing and monitoring tools directly.

The status line shows readiness, battery, free storage and connection or thermal information. A stalled monitor feed displays a warning over the preview.

The preview is used for framing and tap-to-focus. Beneath it, the camera tiles show ISO, shutter, focus, white balance, zoom, exposure compensation, iris, lens, frame rate and look. Some phones have fixed or unavailable controls.

The lower capture area includes light, stabilization, front/back switching, video-format access, the red Record button, the recording timer and microphone information.

H|Use a live adjustment

1. Tap the setting tile. Its adjustment dock opens while the preview stays visible.

2. Drag the slider, or use an available quick preset. Watch the image change and allow the camera state to confirm the new value.

3. Use Auto or the related lock control when available. Tap the tile again or close the dock when finished.

N|The app may briefly show an applying state. If it reports Rejected or unavailable, the device did not accept that setting. Use a supported option rather than assuming the change was recorded.
''', 'Portrait camera workspace. The values and the available hardware controls will differ on another phone.')

add('Camera','Exposure: ISO, shutter, EV and iris','03-iso',r'''
H|ISO and shutter

**ISO** changes sensor sensitivity. Raising ISO brightens the image but can add visible noise. **Shutter** controls how long each frame is exposed. A longer exposure brightens the picture and increases motion blur; a shorter exposure can make movement look sharper or more staccato.

1. Tap ISO or Shutter and move its slider. Manual control requires a camera that exposes manual sensor settings.

2. Set one control, then adjust the other while checking the preview. A shutter time must fit within the selected frame interval.

3. Return to **Auto** when you want automatic exposure again. Custom frame rates that require manual exposure can prevent Auto until you choose a compatible rate.

H|EV and exposure lock

**EV** asks automatic exposure to brighten or darken the scene. It is useful when a bright background makes a face too dark. EV returns exposure control to the automatic path; it is not a second manual ISO control.

**Exposure lock** holds a supported automatic exposure. Set your framing and light first, then lock so the brightness does not drift during an interview.

H|Iris / f-stop

The iris tile shows the actual or supported aperture. If it says **FIXED**, the phone has no adjustable aperture exposed to this app. Only supported variable-aperture values can be selected.

N|For many situations, a shutter near twice the frame rate is a useful creative starting point, not a hardware requirement. Flickering LEDs may require a different tested setting.
''')

add('Camera','White balance and consistent color',None,r'''
H|Use a preset, then fine-tune

White balance changes how neutral surfaces and skin tones are rendered under different light. Open **WB** to choose an available preset, or use the supported manual fine-balance control to shift warmer or cooler. Make small changes while looking at a neutral reference and your subject.

1. Put the subject in the actual light you will use. Let automatic white balance settle before switching into fine manual adjustment.

2. Select a preset that is close to the scene. Adjust the fine control if the image still feels too warm or cool.

3. Keep the setting stable throughout a take. For a supported automatic mode, use **WB lock** once the color looks right.

4. Save the completed setup as a shooting preset if you will return to the same lighting and lens.

H|Understand the limits

Fine balance depends on the camera's manual post-processing capabilities and available color-correction controls. If the app asks you to wait for white balance to settle, allow a moment before trying again. If the control is unsupported, use an exposed preset rather than expecting a hidden adjustment.

The fine-balance control is a warm/cool adjustment based on camera gains. It is not a calibrated Kelvin-and-tint instrument. Two different phones may need different settings to produce a similar look.

H|Do not confuse balance with a look

Balance establishes the neutral base. A look adds a creative treatment. Correct lighting and WB first. Tap the camera **Look** tile, choose an offered preset and adjust its strength while checking skin and highlights. Return to Natural for an untreated starting point. Camera looks require the exposed tone-map controls; a look available in Studio is not guaranteed on every camera. Changing WB can require rechecking exposure and focus.

N|For interviews, avoid mixing strongly different light sources on the face when possible. A single white-balance setting cannot make every differently colored light neutral at once.
''')

add('Camera','Focus, framing, lenses and stabilization',None,r'''
H|Keep the subject sharp

**AF** lets the phone focus automatically. Tap a visible subject in the preview to request focus at that point, if the current lens supports tap-to-focus. The temporary reticle shows where you tapped; it is not an independent guarantee that the subject is sharp.

Use the Focus slider for manual distance when supported. Adjust slowly and inspect fine detail. Enable focus peaking under **Shooting setup > Framing / focus / exposure guides** for a green-edge aid. Peaking responds to preview edges and can also respond to noise; confirm the saved result.

H|Zoom and camera selection

The Zoom slider uses the camera's supported digital zoom range. High digital zoom can make detail softer. Move the tripod or choose an available lens when that produces a better composition.

Tap the **front/back switch** to change camera direction, or use the Lens tile for the cameras Android exposes. Stop the take first. The app cannot guarantee access to every physical lens marketed by the phone manufacturer.

H|Steady the shot

The stabilization button cycles the available off, electronic and optical modes. A device may offer only some of these. Electronic stabilization can crop the view. Recheck framing after changing it, and make the change before recording.

The light button toggles the camera torch if available. Front cameras often have no controllable torch. Use external lighting if the on-device option is unavailable.

N|On a tripod, begin with a steady camera and enough light. Stabilization is a device-dependent aid, not a substitute for secure support or a guarantee of smooth walking shots.
''')

add('Camera','Resolution, frame rate and preview quality','08-quality',r'''
H|Set the recording format

1. Stop recording. Tap the resolution badge or open **Shooting setup > Video resolution / quality**.

2. Choose one of the formats discovered for this camera. The list uses familiar resolution labels and a frame rate. **Maximum available** selects the largest available listed format, which is not necessarily the best choice for a long shoot.

3. Enable **High bitrate / better detail** if you want the higher quality option and have enough storage. Make a test to confirm the camera and encoder can sustain it.

4. Open the **Frame rate slider**, or tap the FPS tile. Select a rate offered for that format. Rates such as 24, 25 or 30 fps appear only when supported by the current configuration.

H|Keep the preview responsive

Under Shooting setup, choose **Preview: Live priority** for the smaller monitoring stream or **Detail priority** for more preview detail. Stop recording before changing this setting. It changes the monitor feed, not the recording's selected resolution and quality.

Higher resolution, bitrate and frame rate increase processing, heat and storage use. Begin at 1080p with a supported standard frame rate before testing higher formats.

N|The format shows requested settings. Use the saved file and a short real-world test to judge delivered quality. This build does not promise 2K, 3K, 4K or high-frame-rate recording on every phone.
''')

add('Camera','Choose and check your microphone','09-microphone',r'''
H|Select the input before recording

1. Connect your USB-C receiver or wired microphone before opening the input list.

2. Tap MIC, or open **Shooting setup > Microphone input / USB receiver**. Choose the intended device or System default.

3. Run a five-second test from Pre-recording check. Speak at the distance and level you will use. Confirm the actual route and the peak reading.

4. Record a short take and listen to it. A visible clap also helps you check a USB receiver's audio/video timing.

H|Voice processing

The camera's **Voice processing** switch requests Android voice processing. The result depends on the phone and microphone. Compare a test with it on and off. It is separate from Studio's offline voice cleanup.

MIC displays dBFS during recording. Values near 0 dBFS indicate a clipping risk; the label turns red near that level. Lower the receiver's gain or move the microphone if necessary. A software control cannot restore clipped sound.

H|If the route changes

Changing the microphone during recording is blocked. If Android routes away from the selected input, the app can stop and save the take with a route warning. Reconnect and reselect the microphone before the next take.

N|The preview stream is not a remote microphone monitoring link. Recordings with sound stay on the camera phone. The Windows webcam feed is video-only; choose a microphone separately in OBS or your calling app.
''')

add('Camera','Pre-recording checks and a sound test','05-preflight',r'''
H|Run the check deliberately

Open **Camera > Shooting setup > Pre-recording check**. Review camera readiness, input availability, free storage, battery, temperature and the chosen format. Charge the phone, free storage or let it cool before a long take if the checks show a concern.

Tap **Test microphone - 5 seconds** on the camera phone. Speak normally while the test shows the actual input route and its peak level. The app deletes the temporary test audio when the check ends or closes. The check does not listen silently in the background.

**Too loud** means reduce gain or change microphone placement. **Very quiet** means check the connection, transmitter, mute button and recording distance. **Signal detected** confirms input, but does not prove that the sound is pleasant or free of echo.

H|A simple repeatable routine

1. Check the tripod and frame. Wipe the camera lens.

2. Check microphone route, gain and battery. Listen to a saved test.

3. Check lighting, white balance and focus. If needed, lock exposure and white balance.

4. Confirm storage and temperature, then start the countdown.

N|Run the sound check while stopped, on the camera phone. A monitor does not run the camera phone's short microphone test remotely. Do not assume a previous successful test covers a newly connected USB adapter.
''')

add('Camera','Countdown, recording and take markers','06-countdown',r'''
H|Give yourself time to get into position

1. Open **Shooting setup > Countdown & sound**. Choose Off, 3, 5 or 10 seconds.

2. Turn Audible countdown on if useful. The setting is saved for the next session.

3. Tap Record. During the countdown, tap Record again to cancel it. Wait for the confirmed recording state before beginning your delivery.

4. While recording, tap the timer to mark a good moment. Hold the timer to mark a mistake or retake point. The same actions are available in Shooting setup.

5. Tap Stop. If stop confirmation is enabled in Event / film tools, choose Stop & save. Wait for the saved confirmation.

H|Use markers later

When you import this recording into Studio, its local marker notes are read with it. Good and mistake indicators appear on the main timeline. Open **Creator tools > Take markers** to jump to them.

Markers are useful for a strong sentence, a new attempt, a clap or an error. They do not cut or delete footage automatically, and they are not printed into the video image.

N|Keep the camera app visible. Backgrounding, locking or leaving Camera attempts to end and save the take. If only the monitor disconnects, recording can continue on the visible camera phone; check that phone directly.
''')

add('Camera','Framing guides, zebras and focus peaking','07-guides',r'''
H|Open the monitoring tools

Use **Shooting setup > Framing / focus / exposure guides**, or hold the Grid icon.

**Rule-of-thirds grid** helps you place a subject. Choose Original frame, Vertical 9:16, Square 1:1 or Landscape 16:9 for a framing guide. This does not change the actual recording resolution or automatically produce multiple crops.

**Social safe area** draws an inset guide. Adjust Right and Bottom clearance to keep important text or a face away from likely app controls. Platform overlays vary; inspect the final upload yourself.

**Zebra** draws stripes on bright preview regions. Adjust its brightness threshold to control where the stripes appear. It is a preview warning, not a calibrated sensor clipping meter.

**Focus peaking** highlights strong edges in green. Use it while adjusting focus and watch subject detail. Compression and image noise can affect the result.

H|Keep monitoring lightweight

These aids analyze a small preview at a reduced update rate. They pause when Android reports severe heat. Disable them when you do not need them, and give the phone time to cool if a thermal warning appears.

N|Guides, zebra stripes, focus highlights and the teleprompter are monitoring overlays. They do not appear in normal camera recordings. Screen recording is different: it captures visible screen content within Android's rules.
''')

add('Camera','Save and recall a shooting preset',None,r'''
H|Save a setup that you have tested

Open **Shooting setup > Shooting presets > Save current shooting setup**. Enter a useful name, such as Talking head - daylight, Product close-up or Interview - USB mic, then Save.

A preset includes the current camera controls, video format, frame rate, quality option, microphone selection and processing, framing aids, countdown, and the script snapshot and layout. It is more useful after you have checked the actual saved video and sound.

H|Recall the preset

1. Connect the microphone you used for the preset.

2. Stop recording and select the original lens. Open Shooting presets and tap the saved name.

3. Wait for the camera to apply the settings. Check for a rejection or unavailable-device message, then recheck preview, sound and framing.

4. If the lighting or receiver configuration has changed, adjust the setup and save an appropriately named preset rather than assuming the old one is correct.

Hold a preset to delete it. Deleting a preset does not delete your camera recordings.

H|Keep expectations realistic

Presets cannot add a missing lens, unlock unsupported frame rates or make a fixed iris variable. They are local to the phone. A preset made for one device is not a universal calibration for another phone or for every room.

N|The script snapshot is part of the shooting preset. Use a deliberate preset name and confirm that the expected script and layout are present before the next take.
''')

add('Camera','Read from the teleprompter','11-prompter',r'''
H|Prepare and save your script

1. Tap the top-bar Teleprompter icon. The reading area appears beside or over the preview, depending on layout.

2. Tap the script-edit icon inside the prompter. Enter a title and write or paste your text. Save it. Open saved lets you choose an existing local script.

3. Open the prompter settings icon to adjust text size and scroll speed. Choose settings that you can read without rushing.

4. Use Play/Pause, Restart and Next paragraph while rehearsing. Mirror the script only if your physical reading arrangement requires reflected text.

H|Link scrolling to recording

In Shooting setup, enable **Scroll when recording starts** if you want the script to begin after recording is confirmed. Pausing the script does not stop the video recording.

Use **Script over preview** if that layout suits your framing. The script is not burned into camera recordings. Place a monitor near the filming lens to reduce obvious sideways eye movement.

N|The prompter uses scrolling controls, not voice-following recognition. Saved scripts are local. The text editor accepts up to 50,000 characters; long scripts are easier to manage as separately titled segments.
''')

add('Camera','Event slates, flicker control and locks','10-event-slate',r'''
H|Name takes before the shoot

Open **Shooting setup > Event / film tools**. Enter a Production / event and Scene / moment. Choose the next take number, then Apply slate. The camera phone uses these details for take naming and advances the next take number automatically.

Use short meaningful names such as Product launch / Demo, Wedding / Speeches or Short film / Scene 03. Set the slate while stopped. It labels the recording; it is not a visual clapboard in the exported image.

H|Protect an important take

Enable **Confirm before stopping a take** if an accidental tap could interrupt an interview or ceremony. You can then choose to keep recording when the stop prompt appears.

Frame and light the subject before using **Lock exposure + white balance**. Release those locks for moving coverage when the light changes. The locks only apply where the current automatic camera modes support them.

H|Reduce lighting flicker

Select the venue's 50 Hz or 60 Hz anti-flicker option in automatic exposure, or test Auto. Available choices depend on the camera.

In manual exposure, use a shutter appropriate for the light and frame rate. Some LED fixtures still need a different shutter or different lighting; the switch cannot eliminate all flicker.

N|For unrepeatable events, check a saved test under the actual venue lights before the main moment. Keep enough battery and storage for the full section you intend to cover.
''')

add('Phone-to-phone monitoring','Pair over Wi-Fi or a phone hotspot','13-monitor-connect',r'''
H|Set up the network first

1. Install Solo Camera on both Android phones. Choose **Camera** on the tripod phone and **Monitor** on the handheld phone.

2. Put both on the same Wi-Fi network. Alternatively, open Pair phones on the camera, choose **Set up camera hotspot** and enable its hotspot in Android Settings.

3. On the monitor, use **Connect to camera hotspot** to join that network. If Android says it has no internet, choose to stay connected.

4. Return to the camera's Pair phones screen for its current QR code. On the monitor choose **Scan QR code**, grant scanning permission and scan that code.

5. Approve the monitor's request on the camera phone. Wait for an updating preview before recording.

H|If QR scanning is inconvenient

The camera offers Manual details with its network address and key. Enter those into the monitor's connection form and approve the request on the camera. Use the address for the shared Wi-Fi or hotspot, not an unrelated network adapter.

N|A router, mobile data and internet service are not required for hotspot monitoring. QR scanning does not join Wi-Fi for you. Keep live codes private; the camera's secure QR screen is intentionally not reproduced in this manual.
''', 'The monitor connection screen. The camera phone separately shows its private, current QR code.')

add('Phone-to-phone monitoring','Control the camera from the monitor',None,r'''
H|What remote control does

Once approved, the monitor shows the camera phone's preview and supported settings. Change ISO, shutter, focus, WB, zoom, EV, looks and other available controls in the same way as on the camera. The camera decides whether a requested change is valid and confirms its state.

Tap Record on the monitor and wait for the confirmed recording indicator. Tap Stop and wait for the saved notice. The video is stored on the **camera phone**, not automatically copied to the monitor.

H|Use scripts remotely

For reading on the monitor, open its Teleprompter and prepare the script there. To also display a script on the camera phone, enable **Allow monitor to display a script here** on the camera and **Send script to camera screen** on the monitor.

Script changes, play/pause and position jumps are sent when you operate a control. Different screen sizes and fonts can produce different reading layouts. Continuous position correction between screens is not implemented, so use one screen as your primary reading reference. Remote scripts are not automatically saved into the camera phone's script library.

H|Connection and recording are separate

If the monitor loses its link, look at the camera phone. It can keep recording while the camera app remains visible. Reconnect explicitly; do not repeatedly tap Record hoping that an old command is pending.

N|One approved monitor client per camera phone is supported. Do not expect the same phone to serve a monitor and a Windows receiver simultaneously. Revoke / new code closes the old authorization and generates a fresh pairing session.
''')

add('Phone-to-phone monitoring','Fix pairing and delayed preview',None,r'''
H|Cannot connect or waiting for preview

Confirm that both devices are on the same Wi-Fi or that the monitor joined the camera hotspot. Return to both apps after changing network settings. Regenerate the camera code and scan the fresh one. Approve the request on the camera.

Avoid guest Wi-Fi that isolates devices. Temporarily disconnect an unnecessary VPN if it prevents local device communication. Move closer to the hotspot or access point. Do not disable security broadly or share the private pairing key publicly.

If the camera preview itself is not updating, fix the camera before troubleshooting the network. Close another app that owns the camera, check permissions, re-enter Camera mode and select a supported video format.

H|Preview feels delayed

Use **Live priority** on the camera. Keep the receiving phone responsive and avoid simultaneous large downloads or uploads on the same Wi-Fi. Update both phones to matching current builds and reconnect after an update. The recording resolution can remain high while monitoring uses a smaller stream.

A local preview still has camera, compression, network, decoding and display delay. This version does not promise zero-delay monitoring. Review the original saved take to separate a preview problem from a recording-quality problem.

H|Safely reset access

Stop recording before changing the network. On the camera choose **Revoke / new code**, reconnect the monitor and approve it again. If only the monitor froze during an important take, verify recording on the camera before closing or restarting the camera app.

N|Keep the camera phone awake and its camera workspace visible. Locking or backgrounding that phone can end a take even though the monitor still shows the last received image.
''')

add('Windows webcam','Connect a phone to the Windows receiver',None,r'''
H|Install the companion pieces

The Windows receiver runs on Windows 10/11 and requires the .NET 10 Desktop Runtime. OBS Studio 28 or newer provides the virtual camera. Run SoloCamera.Windows.exe from its complete extracted folder; do not move only the executable away from its companion files.

H|Pair a camera slot

1. Connect the computer and phone to the same Wi-Fi, or connect the computer to the phone's hotspot.

2. In an available receiver slot, choose **Pair by QR**. If the computer has several adapters, choose the Wi-Fi/hotspot address for the network shared with the phone.

3. On Android choose **Webcam** and scan the Windows code. Confirm Connect on the phone and allow the camera connection when prompted.

4. Wait for the slot to show live video. Repeat for other supported phone slots if needed. QR codes expire after two minutes and are single-use; reopen Pair by QR for a fresh code.

H|Four-slot layout

The receiver offers a single-camera view or a 2-by-2 grid with up to four Android feed slots. Four simultaneous physical-phone operation still needs separate validation. This is not a promise that every phone or Wi-Fi network can sustain four smooth streams.

USB and HDMI cameras are added separately in OBS as Video Capture Device sources. They do not become Android phone slots in the receiver.

N|If Windows asks about network access, allow the receiver on your trusted private local network. This build has no macOS receiver and installs no standalone Solo Camera virtual-camera driver. The Windows workflow depends on OBS.
''')

add('Windows webcam','Show the feed in OBS, Zoom and a browser',None,r'''
H|Set up OBS once

1. In OBS, open **Tools > WebSocket Server Settings**, enable the server and keep authentication on.

2. Enter that port and password in the Windows receiver and select **Connect OBS**. These are local OBS settings, not a social-media account or a paid online service.

3. Choose **Set up webcam scenes**, then select your single camera or grid. Do scene setup while OBS is not recording or streaming.

4. Start the virtual camera. If the receiver cannot start it, click **Start Virtual Camera** directly in OBS.

H|Add a source manually if needed

Copy the receiver's clean-feed URL. In OBS, add a **Browser** source, paste that URL and set its dimensions to the OBS canvas. Enable Control audio via OBS for live screen-demo sound. The local address and session token change when the receiver restarts. Use its reconnect/update-feed option or paste the current URL again. Keep private feed URLs private.

H|Use the webcam in another application

In Zoom or a browser meeting's camera selector, choose **OBS Virtual Camera**. If it is missing, start OBS's virtual camera first, then reopen or refresh the calling application. When a browser asks for camera permission, approve it for the intended site and check that site's video settings.

Keep OBS and the receiver running. Select a microphone separately in the call or in your audio routing. Ordinary camera-webcam mode is video only. Live screen-demo mode can send selected phone audio into OBS, as described in the Screen recording chapter.

N|The receiver does not automatically start OBS recording or a public stream. A connected slot alone does not make a webcam device available to every application; OBS Virtual Camera is the bridge.
''')

add('Windows webcam','Troubleshoot webcam visibility and latency',None,r'''
H|Connected but nothing in OBS

Check the receiver slot first. If it has live video but OBS is blank, verify the selected scene, Browser source visibility and the current clean-feed URL. Reconnect existing OBS feeds after restarting the receiver. Source reconfiguration is blocked while OBS is recording or streaming; stop only when it is safe for your session.

H|OBS works but a call or browser is blank

Start OBS Virtual Camera, select it in the receiving application and check the site's camera permission. Another application may still hold an old camera selection. Reopen the camera selector or refresh the page after the virtual camera starts. Select the desired audio source independently.

H|The picture is behind your movement

1. Select Live priority on the Android camera. Keep the phone and computer close to the same access point or hotspot.

2. Check that the receiver's own preview is updating. If it is late there, address the phone/network path first.

3. If the receiver is responsive but OBS is late, check OBS source/filter settings and system load. Remove an unwanted delay filter rather than changing camera exposure to solve a network problem.

4. If OBS is responsive but the call is late, compare another browser or local camera preview. Conferencing software adds its own processing and network delay.

N|The app uses compressed local preview frames for this workflow. It is not a hardware-encoded, guaranteed zero-latency conferencing link. The updated receiver offers a USB-feed option after local QR pairing; it requires platform-tools and USB debugging. See the screen-demo USB instructions.
''')

add('Screen recording','Record an app demonstration','46-live-screen',r'''
H|Set up capture

1. From Home choose **Screen recording**. Select an output size. The displayed dimensions reflect the phone's screen shape.

2. Enable **Record microphone narration** if you want to explain the demonstration live. App playback can be included in Live screen demo to OBS mode when the demonstrated app permits capture; ordinary local screen recording captures microphone narration only.

3. Enable the presenter option if you want a front/back camera view. See the next page for its extra permission and controls.

4. Tap **Start screen recording**. Approve Android's capture request for this session. Without presenter, annotations or live mode, Android may offer one app or the full display; options vary by system version.

5. Open the app you want to demonstrate and record. Stop from Solo Camera's ongoing notification, Android's capture controls or the Screen recording page.

H|Save and edit

Choose **Stop & save**, then wait for the recording to finish. Use **Play latest recording**, or open Studio to find it with other recordings. Stop before entering the editing workspace through this page.

Files are MP4/H.264 with AAC when microphone narration is enabled. They are saved in **Movies/SoloCamera/ScreenRecordings**.

N|Protected content can appear black. Locking the phone stops and saves the recording. Rotate before starting if orientation matters; rotating during capture can add borders. The long edge is capped at 1920 pixels, with a two-hour or 4 GB session limit.
''')

add('Screen recording','Use the presenter circle and full-screen camera','45-annotation-tools',r'''
H|Choose your presenter setup

Before capture, enable **Show presenter camera - front / back**. Choose the circle size with its slider and turn mirroring on or off. These preferences are set before starting the take.

If Android asks, grant Camera permission and enable **Display over other apps** for Solo Camera. Return to the app afterwards. Presenter mode requests the entire screen so the floating camera can be included while another app is visible.

H|Switch views during one recording

**Drag the circle** to move it out of the way of the control you are demonstrating. Keep your finger down while moving; a short tap is the mode switch.

**Tap the circle** to expand the camera over the display's content area. Speak directly to the audience, then **tap again** to return to the circle and your demonstration. The same recording continues in one file.

The ongoing notification also provides **Camera / demo**. The Screen recording page offers Expand / minimize presenter camera while a presenter session is active.

H|Check the result

Stop and play the saved MP4. Confirm that both the screen and presenter sections are present and that narration is audible if enabled.

N|Android still controls system bars and privacy indicators. The presenter is not a promise of simultaneous front-and-back camera recording. Protected apps or special system screens may hide overlay content. Never record passwords or private information unintentionally.
''')

import runpy
from pathlib import Path
runpy.run_path(str(Path(__file__).with_name('manual_screen_updates.py')),init_globals={'add':add})

add('Studio essentials','Library, projects and saved media','43-project-library',r'''
H|Start or reopen an edit

Studio has a prominent **New project** action. Below it, saved projects appear in a horizontal rail with a title, duration and preview when available. Swipe that rail to browse and tap a card to resume editing.

Recordings and Exports are grouped below the projects. Tap a media tile to play it, then choose Edit this video if you want to begin from that recording. Refresh reloads the library; Storage shows measured local app and device usage.

**Import media** opens the project media panel, with Videos, Photos and Audio. Use the relevant import action to choose files through Android's picker. Media already used in the current project can appear for reuse.

H|Understand the two types of saving

A **project** stores edit instructions and references to source media. It is saved automatically as you work. An **export** is a newly rendered media file that you can play or share without opening the project.

Keep originals available. Renaming, moving, deleting or revoking access to a source can make an existing edit unable to play or export it. Templates also need their referenced source media.

N|The screenshot includes a demonstration project. The library shows app-owned recordings and exports, not a complete replacement for every photo or file manager on your phone. Use Import media for other assets.
''')

add('Studio essentials','Create an empty project and add assets','20-empty-project',r'''
H|You can start before choosing a video

1. Open Studio and tap the large **New project** bar. The app saves a separate draft and opens the empty workspace.

2. Choose **Add video**, **Photo**, **Audio** or **Text**. You do not have to fill every type of track.

3. Choose and confirm your first asset. A visual asset opens the normal editing workspace. Audio added before a visual asset remains in the draft; add a video, photo or text to work in the visual editor.

4. Return to the project library whenever you need a break. Reopen the saved project from its card.

H|Import intentionally

For video and audio, the app provides a preview before confirmation. Play or listen, use Previous/Next if you selected several files, then choose Add to project or Import audio. Cancel returns without adding the auditioned selection.

Photos are selected with Android's image picker. Images are editable visual layers, not text tracks. A separate overlay picker provides Videos and Photos tabs for adding overlays.

N|Import is not the same as inserting into every existing track. Each added object has its own type and timing. Select that object after import to see its correct tools. Do not delete a source file merely because a project thumbnail appeared.
''')

add('Studio essentials','Understand the editing workspace','21-editor',r'''
H|Read the screen from top to bottom

The top bar contains Back to projects, the project name, Undo, Redo, Import media, Creator tools and Export video.

The large preview shows the composite at the current playhead position. Its play button, time display and small keyframe diamond sit beneath it. Use the fullscreen control in the timeline toolbar to inspect the picture more closely.

The timeline toolbar includes Snap, Gaps, time-zoom controls, selection actions, Split and fullscreen access. Swipe horizontally if a toolbar action is off-screen.

The timeline contains the main video row plus only the visual, audio and effect rows that have been added. Objects are colored by type and can show thumbnails or waveforms.

The bottom menu contains Edit, Audio, Text, Overlay, Effects, Canvas and AI Tools. Tap a category to replace that menu with its tools. Use the category's back arrow to return to the parent menu. A tool's adjustment panel appears only when needed.

H|Choose the right target

Tap a timeline object before changing it. Its selection outline and tool heading help identify whether you are editing a video, overlay, text, image, sticker, audio or effect.

N|Pinching the preview changes an object's visual size. Pinching or zooming the timeline changes how much time you see. Neither gesture is the same as trimming a clip's duration.
''')

add('Studio essentials','Play, scrub, zoom and use fullscreen',None,r'''
H|Find a moment accurately

Drag the timeline horizontally to position the white playhead at the desired time. The time display and preview update as you move. Zoom the timeline in for finer timing; zoom out to see more of the project. The plus/minus time-zoom buttons are separate from camera zoom and visual layer scaling.

Use Play/Pause to review the sequence. When the playhead is at the end, return it to an earlier time or the beginning before checking a specific section again. If a media file is unavailable, resolve that source rather than repeatedly tapping Play.

H|Inspect the finished-looking composition

Tap the fullscreen preview control. Use its playback controls if shown and close it or use Back to return to editing. Fullscreen viewing does not render an export or change the project's aspect ratio.

Look at the whole frame and at its edges. Confirm that text is not cropped, an overlay is positioned correctly, and a mask or transition is not hiding important information. Play across edit boundaries as well as within clips.

H|Check sound deliberately

Listen with headphones after adding music, cleanup, pitch changes or a sync correction. If it sounds correct only with phone speakers, test the actual exported file through another playback device before delivery.

N|Interactive preview trades speed against device capabilities. A busy multi-layer project can play less smoothly than a simple export. Save the project and test a short export when you need to judge final output rather than only preview responsiveness.
''')

add('Studio essentials','Split, trim, duplicate and delete',None,r'''
H|Split at the playhead

1. Tap the video you want to edit. Position the white playhead inside it, away from its extreme ends.

2. Tap Split, using the scissors control or the selected object's tools. The object becomes two independently editable sections.

3. Tap the section you want to change. Trim, move, duplicate or delete that section; the other section remains separate.

H|Trim to an exact point

Select the object, place the playhead where it should begin and use **Set start here**. Place it at the desired end and use **Set end here**. **Reset trim** restores the available source range. Timeline edge handles can also be used where displayed; zoom time in before making a fine adjustment.

For audio, the corresponding Music controls are Start here, End here, Reset trim and Split audio. For text, use its text timing controls. Each selected object has its own timing; changing a selected overlay should not trim every other row.

H|Delete safely

Select the intended section and use Remove or Delete selected audio. Check the target type and selection outline before confirming a group deletion. Undo can reverse an edit during the current editing session; do not rely on it after closing or recreating the activity.

Duplicate creates another editable object when that tool is available. Check where the copy starts and which row it occupies.

N|With Gaps OFF, deleting main video closes its empty space. Other layers are independent; recheck their timing after a main-track change. There is a finite project item limit, even though rows appear dynamically as needed.
''')

add('Studio essentials','Move objects between times and rows',None,r'''
H|Long-press, then drag

1. Select an object and hold it until the move state appears. Do not release immediately as though you were only selecting it.

2. Drag left or right to a new timestamp. Move near a visible edge to scroll further through the sequence.

3. For layer types that support it, drag vertically to a compatible row. A new row appears as needed when the object is placed in a new layer position.

4. Release to commit the placement. Play the surrounding section and confirm the object's start and end.

H|Tracks follow object types

Video overlays, text, images, stickers, audio and FX occupy their corresponding kinds of row. They are not interchangeable labels: dropping text into a position does not turn it into a video file. Multiple objects can be arranged over time in a compatible row, and additional rows appear as more objects need layers.

H|Avoid timing surprises

Main-video objects cannot occupy overlapping main-track space; a move may be placed into an available location. When Gaps OFF is enabled, main-video spacing is also compacted. Use Gaps ON if you intentionally need empty time on the main video track.

Use snapping for precise alignment, or turn it off for a free placement. Zoom the timeline until you can see the intended timestamp clearly. Moving an object changes its position in the project, not its visual size on screen.

N|The app limits collections to 300 items, rather than offering unlimited layers. Older or lower-memory phones can become slow well before that limit. Keep a project as simple as the edit needs.
''')

add('Studio essentials','Snapping and main-track gap behavior','23-snapping',r'''
H|Use magnetic alignment

Tap **Snap** in the timeline toolbar. Choose whether magnetic snapping is enabled and whether it targets clip edges, the playhead or the timeline start. Set the snap-distance slider, then Apply.

Snapping is used when you hold and move objects. A wider distance catches nearby anchors more readily. Reduce it or turn snapping off when it prevents the placement you want.

H|Choose the main-track behavior

**Gaps OFF** keeps the main video sequence compact. Main clips roll together to close empty space after relevant edits.

**Gaps ON** allows intentional empty main-video time. Use it when you want precise placements with blank intervals or other visual layers filling that period.

The gaps switch is a project behavior, not the same thing as Snap. Snap aligns a move to an anchor; Gaps OFF changes how the main video is packed.

H|After deleting or moving main footage

Check overlays, text, sound effects, music and FX at the new main edit boundaries. They retain their own timing and are not a promise of linked ripple editing across every row.

N|An empty main-video interval can look black if no visible image or overlay covers it. If you want continuous footage, keep Gaps OFF and inspect your sequence after each removal.
''')

add('Studio essentials','Selection, compounds and undo','24-selection',r'''
H|Select a row or the whole timeline

Hold a track label to open row actions, or use **Select** in the timeline toolbar. Choose Select this track, Select all tracks or Choose individual items. Selection actions let you clear, delete or work with the chosen items.

Check that you selected the intended row. Selecting one audio row should not mean selecting every audio layer in the project.

H|Make a compound composition

Choose **Create compound from all tracks** and confirm the prompt. The app renders the visible composition into a new main clip while keeping an internal source snapshot for unpacking. This takes processing time and working storage; it is not an instant group-only operation.

Select that compound and choose **Unpack selected compound** to restore its source layers. Reset compound trim and speed before unpacking if the app asks. Keep its internal source data available.

H|Undo and Redo

Use the top-bar arrows to reverse or restore edits in the current session. The app keeps a bounded undo history. These arrows do not provide an unlimited archive and are not a substitute for keeping source media and separate exports.

N|After creating a compound, check the canvas and play the rendered result. Group deletion affects selected objects; removing a project object does not necessarily mean deleting its original camera file from the phone.
''')

add('Studio essentials','Add a video or photo overlay','33-add-overlay',r'''
H|Use the overlay picker

1. Position the playhead where the new overlay should begin.

2. Open **Overlay > Add overlay**. Choose the Videos or Photos tab.

3. Select an existing project item or use the import action to choose a file. Preview the video or photo and confirm Add overlay.

4. Tap the newly added timeline object. Confirm that the selected tools identify the correct media type.

H|Place the overlay in the frame

Drag the selected object in the preview. Pinch to resize it or use its Size/Transform controls. Move its timeline block to change when it appears. Trim it or set its start/end at the playhead to change its duration.

Each new overlay can use its own row. Add more layers only when the composition needs them; the app creates rows dynamically rather than reserving many empty ones.

H|Watch the sound

Video overlay sound is muted in the current overlay workflow. Add or detach the needed sound as a separate audio object if you want controlled mixing.

N|An overlay is not the same as a full-screen effect. It is a visual object with its own placement and timing. Select the correct overlay before resizing, cropping, masking or removing it, and check the preview after each change.
''')

add('Studio essentials','Photos, stickers and freeze frames',None,r'''
H|Import and arrange a photo

Use the empty project's Photo action, **Import media > Photos**, or the Photos tab in Add overlay. Choose the image through Android's picker. Its editable object belongs to an Image row.

Select the image to access its applicable transform, crop, animation and keyframe actions. Drag or pinch it in the preview, then adjust the object's timeline duration. It is not a text caption even if an older project had mixed object labeling.

H|Add a sticker

Open the sticker tool under Effects. Choose one of the included symbol/emoji-style stickers and position it. Stickers use their own object type and timeline rows. Available designs are a small built-in set, not an online sticker marketplace.

H|Freeze a video frame

Select main video, place the playhead over a valid frame and choose **Edit > Freeze**. The app extracts that frame and adds a two-second editable Image layer at that point. You can change the image layer's timing and placement afterwards.

A freeze layer covers the underlying video while visible. It does not automatically insert two seconds into the main track, push every other object later or freeze the underlying audio. Move or split other objects deliberately if that is the intended edit.

N|Reverse playback is not exposed as a finished tool in this build. Do not interpret Freeze, Mirror or a negative-looking transition as a full video-reverse function. Mirror flips the picture spatially; it does not reverse time.
''')

add('Studio essentials','Transform, crop, mirror and opacity',None,r'''
H|Resize and position a visual object

Select the object and open Transform. Drag its preview selection to move it. Pinch to scale it. Use Frame size for deliberate scaling and Stretch width/height when you intentionally want non-uniform proportions.

To preserve natural faces and products, keep width and height stretch equal unless distortion is part of the effect. **Reset framing** restores the object's basic placement and sizing controls; check any keyframes that may also be controlling the result.

H|Crop and orient

Crop left/right and top/bottom removes edges from the selected object. Reset crop restores those crop amounts. Rotate and Mirror are available for supported visual selections; use them to orient a clip or flip it horizontally, not to change its duration.

The **Canvas** category changes the whole project's shape: Original, 9:16, 16:9, 1:1 or 4:3. **Fit** preserves the full view and may add borders. **Fill / crop** fills the canvas by cropping edges. Always recheck text and subjects after changing the canvas.

H|Control transparency

Transform's Opacity sets a base visibility amount. An overlay can also have its own opacity control. Opacity keyframes provide changes over time; a base opacity below 100% still limits the final visible amount.

N|Several controls can combine: object scale, overlay size, stretch, crop, mask and keyframes. If a layer seems unexpectedly small or invisible, reset or inspect those controls one at a time instead of repeatedly dragging it.
''')

add('Studio essentials','Masks, feathering and mask placement','29-mask',r'''
H|Shape an overlay

Select the video overlay you want to shape and open its **Mask** tool. Choose None, Ellipse, Rectangle or Linear. Masking reveals only a region of that object; it is separate from background removal.

1. Choose the shape and position the mask with Mask X/Y or by dragging the preview while Mask is active.

2. Resize with Mask width/height or a pinch gesture in the mask tool. The shape can be larger than the original frame.

3. Adjust Mask rotation for the direction you need. This rotates the mask rather than rotating the entire underlying video.

4. Increase Feather to soften the edge. Keep it low for a crisp cutout and higher for a blended overlay.

5. Replay the whole section. Check all edges and moving subjects, not just the paused frame.

H|Recover an unexpected result

Choose Reset mask position to restore its placement, size and rotation. Choose None to remove the mask. If the object is still not visible, check crop, opacity, timing and its place in the layer stack.

N|The screenshot illustrates mask controls. This is a small built-in geometric mask set, not an After Effects-style arbitrary path editor or automatic video subject rotoscoping. Use supported controls rather than expecting an AI cutout from a geometric mask.
''')

add('Studio essentials','Add text and control its timing','26-text-tools',r'''
H|Create and edit a text layer

From an empty project tap Text, or open Text in the editor and choose **New text layer**. Type the caption and confirm. To revise it later, select its timeline object and use **Edit caption**.

Text lives on its own row. Add another text object when a new line or independently timed title needs its own layer. Selecting a text object opens text controls; selecting a photo should open image controls.

H|Position and size

Drag the selected text in the preview. Pinch to resize it, or use Text size and Transform controls. Top, Center and Bottom offer quick vertical placement. Check the final canvas shape before committing a title near an edge.

H|Set the interval

Position the playhead and use **Start text here** or **End text here**. Full project extends the selected text across the project interval. You can also move the timeline object or trim its own boundaries.

To create a separate phrase, add a new text layer rather than rewriting an earlier object that you still need. Delete the selected caption with Remove caption or its applicable Remove action.

N|Text must be active at the playhead to appear in the preview. If you cannot see it, check its time range, selection, color, opacity, position and layer visibility before assuming the import or font failed.
''')

add('Studio essentials','Fonts, colors, background and shadow',None,r'''
H|Make readable titles

Select a text object and scroll within its tool panel to reach the font and style controls. The **Font family** dropdown lists built-in fonts and imported fonts. Use the adjacent left/right arrows to step through families while watching the preview.

Choose Bold or Italic where appropriate, then Left, Center or Right alignment. Text color offers color swatches. Line spacing adjusts the separation of multiple lines. An outline can add contrast, with its own color and width.

H|Use the toggles deliberately

Turn **Background** on to place a colored background around the text. Set its color, padding and corner shape. Turn it off for text without that background.

Turn **Shadow** on for a separate drop shadow. Choose the shadow color, softness and X/Y offsets. A shadow is not the same as the text background; each has its own toggle and controls.

Use enough contrast for a moving shot. A small dark shadow or a restrained background is often more readable than a strong decorative font over a busy image.

H|Import another font

Choose **Import TTF / OTF** and select a licensed font file. Reopen the family selector if needed and choose the imported family. Keep the licensing terms for any font you use in commercial work; importing a file does not grant a new license.

N|The app includes its available font choices, not every font requested in earlier planning. If a specific family is missing, import its legitimate TTF/OTF rather than expecting its name to appear automatically.
''')

add('Studio essentials','Text reveals and particle-style titles','27-reveals',r'''
H|Choose a reveal

1. Select the text object and open Text.

2. Tap **Reveal**, choose one of the 40 named presets and close the list.

3. Move to the start of the text object and play through its duration. A paused frame in the middle does not show the complete reveal.

4. Adjust the object's duration if the reveal feels too slow or too fast. Choose None to remove it.

H|What to expect

The set includes fades, slides, wipes, typewriter-style motion, zooms and particle-style variants such as Sparkle, Confetti, Stardust and Gold dust. These are built-in deterministic effects, not AI-generated particles or separately editable physics simulations.

The reveal follows the text object's time range. Other transforms, opacity controls and animation settings may combine with it. Start with one effect, preview it and only then add more.

H|A useful title recipe

Choose a readable font, limit the wording, place the title inside the safe region and use a short, restrained reveal. Check the text over both bright and dark parts of the clip. A reveal should introduce the message rather than keep it unreadable for most of its duration.

N|The appendix lists all 40 preset names. Some presets share a similar motion family; the names describe choices in this build, not a guarantee of identical behavior to another editing application.
''')

add('Advanced editing','Animate an object with keyframes','31-keyframes',r'''
H|Create a start and end state

1. Select a video, overlay, text, image or sticker object. Place the playhead inside its active interval.

2. Tap the small diamond below the preview to add a keyframe. You can also open Keyframes and use Add / update.

3. Move the playhead to a later moment. Change the object's supported position or scale; once keyframes exist, relevant transform gestures can create or update a key at the new position.

4. In Keyframes, set Position X/Y, Scale, Rotation or Opacity precisely. Play between keys to inspect the movement.

H|Navigate and revise

Use Previous and Next to move between keys, or select a visible diamond. Add / update writes the state at the current playhead. Delete key removes a nearby selected key. Move to an existing key before changing its values if you intend to revise that state rather than add another.

Keep the first animation simple: a small push-in or a short title move. Check that the object is visible for the full movement and that its base opacity does not make it unexpectedly transparent.

N|Keyframes are timed to the selected object. They do not animate every row automatically. Audio tracks do not use these visual motion keyframes. Some base transforms and opacity settings combine with keyed values.
''')

add('Advanced editing','Shape motion with easing curves',None,r'''
H|Change the feel between keyframes

Open the selected visual object's Keyframes tool and choose a key. The curve describes the outgoing motion from that key toward the following one.

**Linear** moves at a consistent rate. **Ease in** starts gently and builds speed. **Ease out** approaches the next state gently. **Ease in/out** smooths both ends. Use a preset first, then adjust the curve handles if you need a custom feel.

H|A simple animated title

1. At the beginning of the text object, add a key with the title slightly below its final position and with low opacity.

2. Move later in the object, add/update a key at the final position and set its opacity to the desired visible value.

3. Return to the first key and choose Ease out for a gentle arrival. Play the title from before its start, not only from the final key.

4. If the movement is too abrupt, increase the time between keys or reduce the distance. Do not add many nearly identical keys when two would work.

H|Distinguish the two kinds of curve

A **motion curve** changes timing between animation states. A **tone curve** in Adjust changes picture brightness and color. Moving a motion handle will not grade shadows, and changing a tone curve will not smooth an object's movement.

N|The current tool is a compact curve editor, not a complete expression engine, motion-capture solver or arbitrary parameter graph. Confirm the animated result in playback and export.
''')

add('Advanced editing','Use animation and transition presets','32-transitions',r'''
H|Animation belongs to one object

Select an object and open Animate. The available choices include Still, Zoom in, Zoom out, Pan right and Fade, with In: Fade, Out: Fade and Combo: Zoom options. Play the full object to judge the movement over its duration.

H|A transition belongs at a cut

1. Place two main video clips directly next to each other. If a gap separates them, close it or move the clips together first.

2. Tap the small transition icon between those clips, or use the applicable Transition after clip action.

3. Choose a visual preset. Adjust Duration, then Apply. Replay from before the cut to see both sides of the transition.

4. To return to a clean cut, reopen the transition and choose Remove or None.

H|Keep transitions useful

Use a restrained fade or dip for a quieter sequence. Whips, zooms, flashes and spins can suit energetic short-form edits, but they should not obscure the subject or become uncomfortable through overuse.

The transition range is limited by the lengths of its neighboring clips. A very short clip cannot support an arbitrarily long transition.

N|The appendix lists the 20 effect choices plus None. These are the app's own implementations, not a claim of exact parity with another editor's presets or every professional compositing transition.
''')

add('Advanced editing','Apply FX over a section of the whole picture',None,r'''
H|Add an independent effect object

Open **Effects > Looks** and choose **Add effect at playhead**. The app creates an FX timeline object rather than permanently assigning the effect only to a selected video layer.

Select the FX object, choose a look and adjust Effect strength. Move its block to change where the effect starts. Trim its interval or use Start at playhead and End at playhead. Add another effect object only when another section needs it.

H|What the FX row affects

The effect grades the visible composite during its active section: main video, overlays, images and text. It is useful when you want a shared treatment over a montage or a particular moment. It is different from a geometric mask on one overlay and different from a title reveal on one text object.

Available look names include Natural, Cinema, Vlog, Warm, Cool, Fade, Golden hour, Teal & orange, Moody, Pastel, Bleach and Vintage. Preview the actual result; a name is a creative starting point, not a guarantee of a particular camera color science.

H|Remove or shorten an effect

Tap the effect's own timeline object, then Remove effect, or trim its duration. If only one section should be affected, do not extend that FX object across the entire sequence by accident.

N|Multiple overlapping FX can combine. If the grade is too strong or text changes color unexpectedly, inspect the FX rows and their intervals before changing every source clip.
''')

add('Advanced editing','Adjust brightness, color and tone curves','28-adjust-curves',r'''
H|Grade a selected video

Select the relevant video or supported video overlay and open Adjust. Move Brightness, Contrast and Saturation in small increments while checking faces, neutral surfaces and highlights.

Scroll inside the adjustment panel to reach **Tone curves**. The graph has Master, Red, Green and Blue channels. Drag a point to change that part of the tonal range and use Curve strength to reduce the treatment.

H|Understand the graph

The horizontal direction moves from darker tones toward brighter tones. Raising a point makes that part of the selected channel brighter; lowering it makes it darker. Master changes overall tone, while individual channels can introduce or correct color casts.

Start with the available S curve, Lift shadows or Soft highlights preset, then refine. Use Reset curves to restore the curve shapes. Reset adjustments restores the basic brightness/contrast/saturation settings; inspect curves separately if a treatment remains.

H|Use a repeatable order

First correct camera white balance and exposure while shooting. In Studio, establish sensible brightness, then contrast, saturation and any subtle curve treatment. Compare the result at a neutral setting before exporting.

N|The screenshot shows the top of the Adjust panel; scroll within it for the curve graph and its channel controls. This is a compact point-based tone tool, not a RAW development suite or an imported 3D LUT manager.
''')

add('Audio','Import, audition and organize audio','34-add-audio',r'''
H|Listen before inserting

1. Open Audio from the editor. Tap **Import audio from device** and select a local file through Android's picker.

2. Use the audio preview to listen before confirmation. Previous/Next moves through a multi-file selection. Choose Import audio to add it, or Cancel to return.

3. The import library lists the project's audio with names, durations and waveforms. An existing sound can be added again at the playhead.

4. Return to the editor and select the new audio object to set its timing, row and level.

H|Build a layered mix

Use separate rows for music, voice-over and sound effects. Additional audio rows appear as you add sounds, so you can move and trim each independently.

The waveform is a visual guide to the recorded energy. It helps locate speech or impacts but is not a replacement for listening, nor is it proof of clean sound.

H|File compatibility

The importer checks for a decodable audio track. MP3, WAV, M4A/AAC and other formats such as OGG or FLAC can work when Android supports the actual encoding. A renamed extension cannot repair an unsupported or damaged file.

N|If a cloud provider returns an incomplete or unavailable file, download a complete local copy first. Unsupported protected content and arbitrary audio encodings are not guaranteed to import.
''')

add('Audio','Trim, split, move and delete sound','35-audio-track',r'''
H|Select the audio itself

Tap its timeline block and check that the controls identify Audio/Music. Do not select a video object if your intention is to remove only a sound layer.

Position the playhead inside the sound and choose **Split audio**. The resulting sections can be edited separately. Use Start here, End here or Reset trim for a deliberate timing change.

Hold and drag the audio block to another timestamp or audio row. Use snapping to align an impact or music change with a visible cut. Add another audio layer for a different sound instead of replacing the previous one unintentionally.

H|Change the level

Audio volume controls the selected sound. Start modestly and listen to the full mix. Several individually acceptable tracks can add together into a distorted mix; boosting above 100% increases clipping risk.

H|Remove only that sound

Choose **Delete selected audio**. Check that the intended object disappears and that the other audio rows remain. Undo can reverse the edit in the current session.

H|Detach sound from main video

Select the main video and use Detach audio from the selection menu or its Volume controls. The app renders the source sound onto its own row and mutes the source clip. Processing may take time; wait for it to finish before splitting or moving the detached sound.

N|Overlay sound is muted in the overlay workflow. If you need it in the mix, add the required audio separately. Detach does not make original files disappear from phone storage.
''')

add('Audio','Voice cleanup and voice-pitch effects',None,r'''
H|Improve a spoken track

Select the intended clip or audio object. In the relevant Voice or Music tools, enable **Voice cleanup - offline** and choose Gentle, Balanced or Strong. Adjust Cleanup strength while listening to the same short section repeatedly.

The cleanup reduces rumble and quieter background sound and helps balance voice level. It runs locally in playback and is included in supported audio and video exports. This is conventional audio processing, not an Adobe Podcast-equivalent AI model.

H|Keep the voice natural

Begin with Gentle. Strong processing can change the tone of speech or make a noise floor more noticeable as it opens and closes. If the room is echoey or the source is clipped, correct the recording setup instead of expecting a slider to reconstruct missing clean speech.

H|Change the character of a voice

For a selected imported or detached audio object, use **Voice pitch** in its Music controls. Move below 1x for a deeper result or above 1x for a higher result; return to 1x for the original pitch. If a video sound needs its own controls, detach it first. This changes pitch; it does not clone a speaker or generate new words.

H|Check the combined treatment

Listen after combining pitch, speed, gain and cleanup. Test the exported file as well as the live preview. Keep an untreated source so you can make a different choice later.

N|Loud background noise, wind, echo and clipping may remain. The best improvement is usually a well-positioned microphone, sensible receiver gain and a quieter recording space.
''')

add('Audio','Record narration while watching your edit','37-voiceover',r'''
H|Record a voice-over take

1. Add a visual asset and position the playhead where narration should start.

2. Open **Creator tools > Record voice-over**, or the Record voice-over action under Audio. Allow microphone permission if requested.

3. Tap **Record / retry**. A three-second countdown gives you time to prepare. The edit plays while you speak; playback sound is muted to avoid feedback into the microphone.

4. Tap Stop. Choose Add voice-over to keep the take, Record / retry for another attempt, or Cancel to discard the unaccepted take.

5. Select the added audio row to trim, move, split, change volume or apply supported cleanup.

H|Narration activity

Accepted takes are marked as narration. When analysis can run, the app stores an activity envelope for music ducking. If analysis was interrupted or the phone was too hot, use the narration controls to analyze later.

The recording stops at the remaining project duration or ten minutes, whichever comes first. Reposition before a new take if you want narration in another section.

N|Voice-over capture uses the system microphone route and is foreground-only. Closing the editor stops and discards an unaccepted take. Keep the app open; this is not background voice-recorder mode.
''')

add('Audio','Automatically lower music under narration','36-ducking',r'''
H|Tell the app which track is which

1. Import or record the spoken audio. Select it and enable **Narration / voice-over**, or mark it as Narration in Creator tools > Music ducking.

2. Allow its local activity analysis to complete. It uses the marked narration's audio levels, not speech recognition.

3. On each music track that should become quieter, enable **Duck this track under narration** or the corresponding Duck switch in Music ducking.

4. Set Reduction and Fade. More reduction makes music quieter during active narration; a longer fade makes the level changes gentler.

5. Play from a quiet interval through speech and back into a quiet interval. Check the exported mix after you are satisfied.

H|Keep roles separate

Use one row for narration and another for music. Do not treat music as the reference voice activity. Narration and Duck are intended for distinct roles; recheck those switches after changing a track's purpose.

H|When the result is unexpected

If music never lowers, check the target music's Duck switch and that a narration track has a completed activity envelope at that time. If music lowers during room noise, improve or trim the narration source and use a less aggressive reduction.

N|Analysis can stop when the editor closes or the phone gets hot. The system is level-based, so noise in the marked narration can trigger ducking. It is not automatic speaker identification or a neural audio-cleaning service.
''')

add('Audio','Calibrate a USB microphone with a clap','38-sync',r'''
H|Make a repeatable sync test

Record a short camera take with the receiver, adapter and microphone mode you intend to use. Show a single clear clap on camera. Import that take into Studio and select its main video clip.

Open **Creator tools > USB microphone sync / clap calibration**. Replay the clip and adjust the correction in 10 ms steps. Positive correction advances audio that is late; negative correction delays audio that is early. The supported range is -1000 to +1000 ms.

Use Play / pause test and Replay selected clip to compare the visible clap with its sound. Make small changes and listen again. Reset correction returns the selected clip to zero correction.

H|Reuse a correction carefully

For an app-recorded take with microphone route metadata, **Save for this microphone setup** stores the correction for future takes from that setup when imported into Studio. Other imported media can store a project-specific correction without a known camera route.

Changing a receiver's mode, adapter or processing can change latency. Re-test after such changes.

N|This is manual calibration. The app does not automatically detect the clap or measure every USB device's latency. It applies the correction in Studio preview/export; it does not rewrite the original capture file or guarantee live webcam audio synchronization.
''')

add('Audio','Find good takes and mistakes in Studio',None,r'''
H|Use markers from the camera

When the app imports one of its marked camera recordings, it reads local sidecar notes. The main timeline displays good/mistake indicators at their source-relative positions.

Select the relevant main clip and open **Creator tools > Take markers**. Tap an entry to jump to that moment. Watch and listen, then split or trim deliberately. A mistake marker is a review aid, not an instruction that automatically deletes a sentence.

H|Add or remove markers while editing

At the current playhead, choose Mark good moment or Mark mistake. The marker belongs to the selected main clip. Hold a listed marker to remove it. Use Undo when you need to reverse a recent marker edit.

H|What stays aligned

Markers are stored as source timestamps, so they remain meaningful through trims and speed changes. A marker outside the selected clip's visible source range is not a currently visible edit point. A copied or split section can retain its relevant source markers.

H|What does not travel with a shared file

These marker notes are local app metadata. Copying only an MP4 to another phone does not automatically transfer the marker sidecar or a project timeline. Exported videos do not show marker symbols in the picture.

N|If you move a take to another device for editing, plan the media/project handoff separately. Do not rely on local marker notes as the only record of a critical production decision.
''')

add('AI and reusable setups','Use optional AI without bundling large models','39-ai-tools',r'''
H|The supported optional tools

Open a visual project, select the intended object and choose **AI Tools**. Diamond-marked actions identify optional AI functions. In this build, the supported person model is used for photo person cutout and person framing/tracking in a main video.

If the model is absent, the action asks whether to download it. The prompt states the internet requirement and the approximate model size, **249 KB**. Choose Download to proceed or Cancel to leave without using the effect.

H|First use and later use

The model is stored privately in the app and reused after download. The implementation also depends on a compatible Google Play services TensorFlow Lite runtime. First-time runtime preparation can require connectivity; phones without that runtime may fail even if the model file is present.

The model size is not total installed storage or processing memory. The effects check memory and heat before running and use one CPU thread. Let the phone cool and close unnecessary apps if the tool asks.

H|Manage the download

Use Remove downloaded model when you no longer need it. Applying the effect again can prompt for another download. Cancelling a download does not enable the effect with a partial file.

N|Automatic captions, translation, video background replacement, face smoothing, neural audio cleanup, AI voice generation and a general AI assistant are not provided as working downloadable packs in this version.
''')

add('AI and reusable setups','Remove a person photo background',None,r'''
H|Apply cutout to a selected image

1. Import a photo as an Image layer in your project. Select its timeline object, not a text caption or the main video.

2. Choose the diamond-marked **Background remover** action from its available tools or AI Tools.

3. If prompted, review the approximate model size and internet requirement. Download the model, or Cancel if you do not want it installed.

4. Let the image process. Inspect hair, hands, clothing edges and any semitransparent area against the background of your composition.

5. Reposition or resize the resulting image layer, and preview it over the intended underlying content.

H|Choose suitable source material

The included model is **person segmentation**. It works best when a clearly visible person is separated from the scene. It is not a general product-background remover and should not be expected to isolate every object, animal or complex transparent subject.

If the edge is poor, try a clearer photo with more separation and less motion blur. Keep the original so that you can change the treatment or choose another image.

H|If processing fails

Check that the model downloaded completely, the runtime is available, the source photo can be read and the phone is not too hot or low on memory. A small model does not guarantee a perfect cutout.

N|This tool processes a photo. It is not continuous video background removal, a geometric mask, a live webcam replacement or a universal green-screen engine.
''')

add('AI and reusable setups','Track a person with editable framing keys',None,r'''
H|Use tracking on a main video clip

Select the desired main video clip and open **AI Tools > Track person - selected video**. If necessary, accept or cancel the optional person-model download. Tracking uses the same model as person photo cutout, so it does not require a second model pack.

Read the confirmation before starting: the tool samples up to **12 frames** and replaces the selected clip's existing motion keys with editable pan/zoom keys. Keep the current keys or an earlier project state if you do not want to lose that animation.

H|Review and correct the result

After processing, play the whole selected clip. Inspect any moment where the person turns, leaves the frame, is obscured or moves rapidly. Open Keyframes to move, scale, remove or add keys where the automatic framing needs correction.

A result with few usable points may not track convincingly. Use a clip with one clearly visible person and enough separation from the background. It is a framing aid, not a promise that every face stays perfectly centered.

H|Understand the scope

The tool follows a visible person using sparse analysis. It is not arbitrary object tracking, skeletal motion capture, a 3D camera solve, stabilization, a live autofocus replacement or frame-by-frame video rotoscoping.

N|Tracking can alter your intended composition. Review the edges, text and overlays after applying it. Low-memory or overheated devices can refuse processing; avoid starting it immediately before an important camera take.
''')

add('AI and reusable setups','Save a creator template for the next edit','42-templates',r'''
H|Save the project as a starting point

Prepare the titles, logo, intro/outro, framing and layer arrangement you want to reuse. Open **Creator tools > Creator templates**, or Creator templates in Studio's library. Choose **Save this project as a template**, enter a useful name and save.

H|Start a separate edit from it

1. Open Creator templates and choose the saved template.

2. The app creates a **new independent project**. Check its name and timeline before replacing or adding media.

3. Keep reusable titles and branding. Remove demonstration footage you no longer need and add the new take deliberately.

4. Review the canvas and export size; those project settings are reused too.

Hold a template to remove it. Removing the template is different from deleting its referenced original media or an already-created project.

H|Preserve the assets

Templates reference existing media on this phone. They are not automatically self-contained zip archives and do not guarantee that another device has the same files or file permissions. Retain reusable logo, title and intro/outro sources.

N|A shooting preset restores camera setup. A creator template starts an editing project. They solve different problems, and neither replaces a separate backup of important recordings and final exports.
''')

add('Export and delivery','Export a finished MP4 video','40-export-video',r'''
H|Prepare the project

Play the beginning, several cuts and the ending. Check text, images, overlays, masks, FX and the full audio mix. Set the intended canvas ratio before exporting.

1. Tap **Export video** in the editor's top bar.

2. Enter a useful project/output name. Choose the available 720p or 1080p quality option. The canvas shape determines the other output dimension.

3. Tap **Export MP4**. Keep Solo Camera visible. Wait through rendering and the final saving stage rather than closing at the end of the progress bar.

4. When Export saved appears, choose Play to verify it, Open / share to hand it to another app, or Done to return to editing.

H|Where it goes

Video exports are saved in **Movies/SoloCamera/Exports** as MP4 with H.264 video and AAC audio when present. The app finalizes the media entry and requests a media scan so gallery/importing apps can discover it.

The export is a new file. Your project remains editable, and its originals are still needed for later re-editing.

N|This build offers 720p/1080p editor export choices, not every camera recording format or an arbitrary codec/container picker. A source recorded at 4K does not mean this dialog promises a 4K export. Closing the editor cancels export and retains the draft.
''')

add('Export and delivery','Export WAV, MP3 or M4A audio','41-export-audio',r'''
H|Choose the format for the next app

Open Export video, then **Export audio**. Choose one of the available formats:

**WAV - 16-bit PCM** is a useful starting choice for another editing or enhancement tool. It is larger, but avoids another lossy compression stage at this export.

**MP3 - 192 kbps** creates a smaller, widely supported file. It is appropriate when the destination accepts MP3 and file size matters.

**M4A - AAC** is a compact option for destinations that accept AAC in M4A.

Tap Export and keep the editor open until it finishes. Use Open / share or choose the resulting local file from the receiving application's picker.

H|Know which sound you are exporting

Audio-only export is the project's supported audio mix; it is not automatically a solo export of whichever audio object you last tapped. Mute or remove unwanted sources in a deliberate working edit before exporting a voice-only version. Check music, detached source sound and any remaining main-video audio.

H|Find the file

Audio exports are saved in **Music/SoloCamera/Exports**. Use the export completion dialog to share directly if another app does not list the file immediately.

N|If an enhancement service rejects a file, try WAV or its documented accepted format, verify the complete export and select it from local storage. Merely changing an extension does not convert the encoding.
''')

add('Export and delivery','Plan a post as a local draft','18-post-composer',r'''
H|Prepare your content now

From Home choose **Schedule & publish**. Queue shows local drafts separately from any service-confirmed jobs. Tap **New post** to choose the exported video and write a caption.

Select the intended channels and adjust any channel-specific fields shown in the composer. Choose a planning date/time if useful, then **Save draft**. The app stores this draft on the phone.

In Queue, use Edit to revise it or Delete to remove the local draft. Calendar groups dated local drafts separately from confirmed schedules. Check the phone's date, time and time zone when planning a post.

H|What this does not do yet

The current build displays **Social publishing - Coming soon**. Account connection and automatic posting to Facebook, LinkedIn, TikTok and YouTube are not enabled in the normal creator setup.

Saving a draft with a date does not reserve a slot on a social platform and does not mean it will post when the phone is locked. A successful MP4 export is also not a public upload.

H|Publish manually meanwhile

Export the final file, open the intended social app or site, choose its upload action and select the exported video. Paste your prepared caption and use that platform's own posting or scheduling options if available.

N|Do not wait for a local Solo Camera draft to publish automatically in this version. Confirm the post in the social platform itself when delivery matters.
''')

add('Export and delivery','Accounts, calendar and publishing status','16-publish-accounts',r'''
H|Read the account screen correctly

Accounts lists Facebook Pages, LinkedIn, TikTok and YouTube, with connection actions marked Coming soon when the publishing service is not configured. This is a feature-readiness limit, not a problem with your password.

You do not need to search for an HTTP service, buy a separate hosting plan or paste developer secrets into a creator-facing field to use the rest of the app. Account connection is not something a creator can complete by inventing a server address.

H|Calendar versus a confirmed schedule

Calendar can show dated **local drafts**. A real scheduled post requires confirmed platform delivery through an enabled publishing workflow. Queue distinguishes on-phone drafts from scheduled and published jobs; use the labels rather than assuming that a chosen date equals successful scheduling.

H|When account connection becomes available

Use only the app's official connection flow and the platform's legitimate sign-in/permission page. Review the requested destination and permissions. This manual does not provide a working sign-in path for a feature that is still disabled in build 0.52.1.

N|The developer-facing service settings present in a debug build are not a normal creator setup step. Platform credentials, app review and deployment are owner/developer responsibilities, not requirements to impose on every creator.
''')

add('Export and delivery','Storage, backups and source-file safety',None,r'''
H|Know where your work lives

**Camera recordings:** Movies/SoloCamera. **Screen recordings:** Movies/SoloCamera/ScreenRecordings. **Video exports:** Movies/SoloCamera/Exports. **Audio exports:** Music/SoloCamera/Exports.

Projects, imported working assets, generated frames, template data, font imports and model files can also occupy app-private storage. The APK file size is only the application package, not the full editing footprint. Video cache and exports can exceed it by gigabytes.

H|Use Storage in Studio

The Storage screen reports measured APK/split payload, projects and freeze frames, temporary cache, optional model storage, free space and available RAM. A phone's total RAM is not all available to this app, and a small model file can still need substantial processing memory.

H|A safe backup routine

1. Verify the original takes and final export by playing them.

2. Copy important original and finished media to a separate storage location you control.

3. Keep the editable project and required sources until the edit is delivered and any revisions are complete.

4. Remove only files you have positively identified as no longer needed. Do not clear app data or uninstall as a casual way to free space: local scripts, projects, templates and downloaded models can be lost.

H|Manage heat and battery

Use sensible capture sizes, Live priority monitoring and only the overlays you need. Avoid editing/AI processing while already overheated. Pause intensive work and let the device cool. A short successful test is not a long-session battery or thermal guarantee.

N|There is no complete user-facing project-package/cloud-backup workflow in this build. Sharing only the final MP4 preserves the finished result, not an editable multi-layer project.
''')

add('Practical workflows','A talking-head video from shoot to export',None,r'''
H|Before recording

Set a stable 1080p format and a supported frame rate. Choose your microphone, run the sound check and record a clap test if using a receiver with uncertain timing. Set white balance for the actual light and check focus. Save the setup as a shooting preset once tested.

Open your titled script in the teleprompter. Use a comfortable text size and speed, enable a countdown if needed and place your reading screen near the lens. Pair a monitor if it helps you frame and operate alone.

H|During the take

Wait for confirmed recording. Speak in manageable sections. Mark a strong attempt with a tap on the timer or a mistake with a hold, then continue if you want to preserve the full take. Stop and wait for the saved message before leaving Camera.

H|In Studio

Play the original. Create a new project or edit that video, then use markers to find good sections. Split around mistakes, remove the unwanted portions and recheck continuity. Add a restrained title, logo and relevant overlay rather than filling the frame unnecessarily.

Add quiet background music on its own row. Mark the spoken reference as narration where needed and enable Duck on the music. Apply gentle cleanup only after listening. If receiver timing is wrong, use clap calibration on the selected main clip and review the corrected export.

H|Deliver

Choose the final canvas, check safe placement, export MP4 and play it once outside the project. Share or upload the finished file through the intended social app. Keep the source, project and export until the job is complete.
''')

add('Practical workflows','A narrated app demonstration',None,r'''
H|Prepare the screen

Close private conversations and make sure no passwords, payment details or confidential notifications will appear. Arrange the app you intend to demonstrate before capture. Choose a smaller recording size if clarity remains sufficient and processing is a concern.

H|Capture the explanation

Open Screen recording. Enable narration if you want live explanation. Enable the presenter only if it serves the lesson, set its circle size/mirroring and approve the overlay permission if Android asks.

Start capture and grant the current Android screen-recording approval. Demonstrate one task at a time. Drag the presenter circle away from the controls. Tap it for a direct-to-camera explanation and tap again to return to the circle. Both modes remain in one recording.

H|Refine the lesson

Stop & save, play the result, then open it in Studio. Cut waiting time and mistakes with the playhead and Split. Use short text callouts where they clarify an action. Add screenshots or another video as overlays only when they help the explanation.

If live narration needs replacing, mute the original clip sound and record a new voice-over while watching the edit. Keep any music low and use ducking when appropriate. Check that UI text is still readable after the chosen canvas crop and export size.

N|App audio can be captured in live OBS screen-demo mode if the demonstrated app permits it. Ordinary local capture uses microphone narration only. Protected screens can be black. Test the exact demonstrated app before planning a tutorial that depends on its video or audio content.
''')

add('Practical workflows','Event coverage and a short highlight edit',None,r'''
H|Before the occasion

At the venue, make a real camera and microphone test. Set Production/event and Scene/moment in Event / film tools. Check anti-flicker under the actual lights, select a sustainable recording format and consider stop confirmation for unrepeatable moments.

Recordings are saved locally on the camera phone. Monitoring is not a backup or an automatic media-transfer system. Plan battery, storage and a separate media backup before relying on the setup for an event.

H|Capture in useful sections

Use clear take names for speeches, performances, cutaways and details. Mark useful moments while recording. For interviews under fixed lighting, exposure/WB locks can help consistency. Release them when moving into different light.

H|Build the highlight

Import the chosen takes into a new project. Arrange the main story first, then trim. Keep overlay cutaways, titles, music and voice-over on appropriate independent rows. Use a small number of transitions with a clear purpose. Add an FX section or subtle grading only after the story works without it.

Choose 16:9 for a landscape delivery or 9:16 for a vertical version and reframe intentionally. Cropping an event shot can remove people at the edges; do not assume a central crop suits every moment.

H|Finish reliably

Export and inspect the start, end, speech clarity and every key transition. Keep original media, source projects and delivered files until the event owner has reviewed the result. Avoid changing the app or phone configuration immediately before a high-value recording.
''')

add('Troubleshooting','Camera controls or recording do not work',None,r'''
H|Camera unavailable or black preview

Check Camera permission and close another application that may own the camera. Return to the Camera workspace. Choose a supported standard format and allow the device a moment to configure. If a specific lens/format combination repeatedly fails, try another listed format and make a saved test before relying on it.

H|Iris is fixed, or a slider is unavailable

This normally reflects hardware capability exposed by Android. Fixed aperture is not sensitivity. Adjust light, ISO, shutter or automatic-exposure compensation as appropriate. A manufacturer may restrict manual focus, fine WB or extra lenses on a particular device.

H|A new setting was rejected

Read the notice. Stop before changing lens, stabilization, quality or microphone. A shutter must fit within the frame interval. Some custom frame rates require manual exposure; select a compatible standard auto-exposure rate before enabling Auto again.

H|The take stopped unexpectedly

Check whether the app was backgrounded, the screen locked, the microphone route changed or storage ran low. Look for the saved take before retrying. In Shooting setup on the camera phone, Recover interrupted takes can publish readable app-owned pending videos. It cannot reconstruct an MP4 whose necessary recording metadata was never finished.

H|Flicker, noise or motion blur

Test venue anti-flicker settings and shutter, add light, lower excessive ISO, and use a secure support. A saved test under actual lighting is more useful than copying a setting from a different room.

N|Do not clear app data as a first fix. If a problem persists, record the app version, phone model, lens, format, microphone route and exact error before seeking help.
''')

add('Troubleshooting','An object is missing, wrong or hard to edit',None,r'''
H|The object is not visible

Move the playhead inside its active interval. Select it and inspect position, scale, crop, mask, opacity, animation and keys. Check whether another visible layer covers it. Very low base opacity and opacity keys can multiply into an almost invisible result.

H|The wrong tools appeared

Tap the intended object on its own row and read the tool heading. Use the menu back arrow to return to the parent category. Text, Image, Sticker, Audio and FX are distinct object types. Do not use a text panel to diagnose a selected main video.

H|Moving or trimming seems to affect another layer

Confirm the selection outline and any multi-selection. Clear group selection and choose only the intended object. Use its own boundaries or timing controls. After a main-track edit with Gaps OFF, recheck independent rows for timing alignment.

H|I cannot place a clip at a precise time

Zoom time in. Hold until move mode starts, then drag. Disable or reduce snapping if it catches an unwanted anchor. Use Gaps ON for intentional main-track empty space; Gaps OFF compacts main video.

H|An overlay or edit causes a long pause

Give a large source a moment to open, then simplify the project if necessary. Test a lower-resolution source and fewer simultaneous layers. Keep adequate working storage and let an overheated phone cool. Do not repeatedly trigger imports or processing while the device is already busy.

N|If you have a repeatable crash, preserve the sources and a description of the last action. Undo and auto-save are useful but cannot guarantee recovery from every process or device failure.
''')

add('Troubleshooting','Audio import, silence, noise and sync problems',None,r'''
H|A file will not import

Choose a complete local copy. If it came from a cloud drive or messaging application, make sure it actually downloaded. Check whether it contains a supported audio track rather than relying only on its filename. Try a known-good WAV, MP3 or M4A/AAC export. Protected, damaged or unsupported encodings can fail.

H|I cannot hear an imported sound

Place the playhead inside its timeline interval. Check its volume, trim range and start time. Confirm the phone's output route and media volume. A video overlay's sound is muted by design; add the needed audio independently.

H|Narration sounds distorted

Lower receiver gain and test microphone placement. Try lighter cleanup and remove extreme pitch or speed changes. If distortion is in the original, processing cannot reliably restore missing unclipped detail. Listen on headphones and to the exported file.

H|Music will not duck

Mark the spoken reference as Narration and let its activity analysis finish. Enable Duck on the separate music track. Check that narration is active at the same time. Analysis can pause when the editor closes or the phone gets hot.

H|USB sound is behind or ahead of the picture

Record a visible clap with the actual receiver setup. In Studio's microphone-sync tool, advance late audio with a positive value or delay early audio with a negative value. Test small changes, save the appropriate microphone setup correction and export a short sample. Recalibrate if you change an adapter or receiver mode.

N|The manual sync tool does not automatically repair every external-microphone recording or align a separate live OBS microphone. Those paths need their own verified timing setup.
''')

add('Troubleshooting','Export errors and missing files in another app',None,r'''
H|Export failed, including muxer/no-sample errors

Keep the editor visible and check working storage and temperature. Make sure every source can play locally. Test a short simple sequence at 720p, then add layers back to identify the problematic source or effect. Stop issuing repeated export requests while one is still being cancelled or saved.

An error such as **7002 - No output sample written in the last 10,000 milliseconds** indicates a stalled export path, not a request to type a different filename. Preserve the project, try a small known-good export and record the exact failing composition and message if it repeats. The app cannot guarantee that a retry fixes every codec or device issue.

H|The export is in Files but not another editor

Confirm the Export saved message and play the completed file. In the receiving editor, check Android's photo/video access permission and the selected import location. Use Open / share from Solo Camera's completion dialog, or choose the actual MP4 through the receiving app's file picker.

H|An audio-enhancement tool rejects the output

Export WAV first if it supports PCM audio. MP3 and M4A are alternatives only when the destination accepts their actual encoding. Select the full local file; renaming .m4a to .wav does not convert it.

H|The picture looks squeezed or cropped

Inspect the project Canvas ratio, Fit/Fill, per-object stretch, rotation and keyframes. Test the exported file in a rotation-aware player. Reset unintended stretch rather than changing the export name or expecting a container change to repair geometry.

N|Do not delete the original or project until the receiving app can open the finished file and you have reviewed the result.
''')

add('Troubleshooting','Screen capture, optional AI and publishing',None,r'''
H|Presenter circle is missing

Enable presenter mode before capture and allow Camera plus Display over other apps for Solo Camera. Approve the new entire-screen capture request. Check that a protected screen is not hiding the overlay and that another app is not holding the camera. Tap versus drag deliberately when switching modes.

H|Screen capture is black or lacks app sound

Protected apps can prevent capture. Other applications' internal audio is not included in this version; enable microphone narration for your explanation. Locking the phone stops/saves the session. These are not problems that a larger AI model will solve.

H|An AI download or effect fails

Check internet access for the download and first-time runtime preparation. Cancel/retry an incomplete download using the app controls. Confirm compatible Google Play services, readable source media, sufficient memory and a cool device. Person cutout is not a general object extractor; person tracking is sparse framing analysis, not continuous camera tracking.

H|Account connection says Coming soon

That feature is not enabled in this build. A creator does not fix it by buying a server, entering an API key or supplying developer credentials. Save drafts if useful, export the finished video and publish through the intended platform's own app meanwhile.

H|Local draft did not post at its chosen time

It was a planning draft, not a service-confirmed social schedule. Confirm publication directly in the platform. Queue and Calendar labels distinguish local planning from actual scheduled/published jobs.

N|When a tool is explicitly marked unavailable, treat it as unavailable rather than searching for hidden settings. This guide documents current behavior, not every feature discussed as a future idea.
''')

add('Quick reference','Supported tools and current boundaries',None,r'''
H|Available in this build

Camera and one approved Android monitor; QR/manual local pairing; hotspot workflow; capability-based camera controls; manual fine WB where supported; recording presets, countdown, take markers, scripts and preview guides.

Windows receiver with four feed slots, a single/grid output and an OBS-based virtual webcam path. Screen capture with microphone, front/back presenter switching, pointer/pen/highlighter, and live screen/selected-audio delivery to OBS with a local saved copy.

Separate Studio with saved projects, dynamic typed rows, playhead editing, split/trim/move, selection, compounds, undo/redo, overlays, images, stickers, text styling, motion keys/curves, geometric masks, transitions, looks, tone curves, audio mixing, voice-over, cleanup, ducking, sync correction, templates and MP4/audio exports.

Optional person photo cutout and sparse person-framing tracking, with an opt-in model download and compatible runtime requirements. Local publishing drafts and calendar planning.

H|Do not assume these are available

Full automatic social publishing/account connection; a macOS receiver; an independent Windows camera driver; relayed webcam microphone audio; automatic four-phone synchronization; automatic monitor-to-camera media transfer; complete portable project packaging; arbitrary video reverse; unlimited layers; arbitrary path masks; 3D camera tracking; skeletal motion capture; video background replacement; face smoothing; automatic captions/translation; voice generation; a general AI assistant.

H|Device-dependent

Extra lenses, variable aperture, manual sensor controls, fine WB, stabilization, maximum resolution and available FPS combinations. A supported UI control on one phone does not establish support on another.

N|The editor is a lightweight creator tool with its own feature set. Familiar workflows do not mean that every function or exact effect of CapCut, Premiere, DaVinci Resolve or After Effects is implemented.
''')

add('Quick reference','Transition preset directory','32-transitions',r'''
H|Clean and light-based

**None** keeps the cut clean. **Black dip** darkens into the cut. **White flash** and **Warm flash** add bright white or warm light treatment.

H|Directional motion

**Whip left**, **Whip right**, **Whip up** and **Whip down** move the transition in the named direction. **Wipe left** and **Wipe right** use a moving reveal.

H|Scale and rotation

**Crash zoom**, **Zoom pull**, **Tilt punch**, **Spin clockwise** and **Spin counter** provide energetic zoom/rotation treatments.

H|Shape and rhythmic motion

**Bounce**, **Swing**, **Squeeze X**, **Squeeze Y**, **Iris** and **Shutter** provide the remaining movement/shape choices.

H|Apply them consistently

Use the icon between adjacent main clips, choose a tile and set Duration. The app offers 20 effect choices plus None. Actual duration is constrained by neighboring clip lengths. Remove returns to an untreated cut.

N|A transition is centered around a cut. An FX object covers a chosen timeline section, and an animation affects one object over time. Select the appropriate tool instead of using those terms interchangeably.
''')

add('Quick reference','Forty text reveal choices',None,r'''
H|1-10: basic entrances

1 Fade; 2 Rise; 3 Drop; 4 Slide left; 5 Slide right; 6 Slide up; 7 Slide down; 8 Pop; 9 Zoom in; 10 Zoom out.

H|11-20: wipes and expressive motion

11 Typewriter; 12 Wipe left; 13 Wipe right; 14 Wipe up; 15 Wipe down; 16 Iris; 17 Soft reveal; 18 Bounce; 19 Elastic; 20 Blur-like fade.

H|21-30: stylized and particle variants

21 Flicker; 22 Glitch; 23 Wave; 24 Flip; 25 Spin; 26 Sparkle; 27 Confetti; 28 Fireflies; 29 Stardust; 30 Burst.

H|31-40: decorative particle variants

31 Comet; 32 Bubbles; 33 Snow; 34 Hearts; 35 Stars; 36 Pixel dust; 37 Neon sparks; 38 Gold dust; 39 Color pop; 40 Particle sweep.

H|Where to choose them

Select a text object, open Text and tap Reveal. Choose None to remove its reveal. Play through the object's full active interval to judge the effect; a paused middle frame cannot communicate its motion.

N|These are the named built-in choices in 0.52.1. Some share underlying motion families. They are not AI-generated title videos, stock-footage particle packs or a promise of independently editable particle emitters.
''')

add('Quick reference','Fonts, looks and format directory',None,r'''
H|Text families in the selector

Sans, Serif, Monospace, Condensed, Casual, Cursive, Sans light, Sans medium, Inter, Montserrat, Poppins, Bebas Neue, Playfair Display and Lora. Imported TTF/OTF families appear as imported entries. Device font rendering and style availability can vary; verify the actual preview and export.

H|Named color looks

Natural, Cinema, Vlog, Warm, Cool, Fade, Golden hour, Teal & orange, Moody, Pastel, Bleach and Vintage. Camera looks depend on camera tone-map support. Editor FX operate over their selected composite interval. Similar names do not establish identical processing on the two paths.

H|Canvas ratios

Original, 9:16, 16:9, 1:1 and 4:3. Fit preserves the whole source with possible borders. Fill/crop fills the shape and can remove edges. A framing guide in Camera is separate from actually setting Studio's canvas.

H|Export choices

Editor video: MP4, H.264, 720p/1080p choices with the selected canvas geometry; AAC audio where present. Audio-only: WAV 16-bit PCM, MP3 192 kbps or M4A/AAC. Camera recording formats depend on device capability. Screen recording uses actual screen-shaped output dimensions, capped at a 1920-pixel long edge.

N|Import/export compatibility depends on actual codec and file contents, not just an extension. Preserve source quality and avoid unnecessary repeated lossy exports before final delivery.
''')

add('Quick reference','Creator glossary: capture and sound',None,r'''
**ISO:** camera sensitivity setting. Higher values can brighten an image and increase noise.

**Shutter:** exposure time for each frame, affecting brightness and motion blur.

**FPS:** frames per second. The available capture rates depend on the selected camera format.

**EV:** exposure compensation for automatic exposure. It asks the automatic system to bias brighter or darker.

**Aperture / iris / f-stop:** physical lens opening. Fixed-aperture phones cannot vary it through software.

**WB:** white balance, the camera's neutral-color adjustment for the lighting.

**AF / manual focus:** automatic versus user-set focusing. Peaking is a visual edge aid, not the focusing mechanism itself.

**Bitrate:** the amount of encoded data per unit time. More can preserve detail but costs storage and processing.

**dBFS:** digital sound level relative to the maximum digital signal. Near 0 dBFS means little or no headroom before clipping.

**Clipping:** loss/distortion when picture highlights or sound exceed their usable range. Cleanup cannot reliably reconstruct clipped original audio.

**Ducking:** lowering selected music under marked narration activity, then restoring it.

**Sync offset:** a deliberate timing correction between picture and sound. Positive correction in Studio advances late audio; negative delays early audio.

**Preview latency:** delay between a live event and its displayed monitoring image. It is not necessarily the timing or quality of the saved source file.
''')

add('Quick reference','Creator glossary: editing and delivery',None,r'''
**Asset:** source video, photo, audio or other material used in a project.

**Project:** editable instructions and source references. It is not the same as an exported media file.

**Timeline / track / row:** the time-based workspace and its separate media-layer lanes.

**Playhead:** the white time-position line used for preview, split points and other time-based operations.

**Split / trim:** dividing an object into sections versus changing its visible/audible source range.

**Overlay:** a separately positioned visual layer over the main composition.

**Mask / feather:** a shaped visibility region and the softness of its edge.

**Opacity:** visibility/transparency amount. Base opacity and keyed opacity can combine.

**Keyframe / easing curve:** a saved state at a time and the way movement progresses toward the next state.

**Tone curve:** a brightness/color mapping, separate from motion timing.

**Compound:** a rendered composition used as one clip, with an internal source snapshot for unpacking in this app.

**Template / shooting preset:** an editing starting point versus a saved camera setup.

**Export / local draft / published post:** a rendered file, on-phone post planning, and actual content accepted by a social platform. Do not treat these as equivalent states.

H|Version note

This guide covers Android 0.52.1/build 58 and the Windows receiver workflow present alongside it. UI labels, platform dialogs and hardware support can change in later releases. Use the matching app-version manual when substantial changes arrive.
''')

runpy.run_path(str(Path(__file__).with_name('manual_recent_updates.py')),init_globals={'add':add})
