# Solo Camera creator suite — v0.49

All ten creator additions work locally. No models, subscriptions or network calls were added. Existing optional font downloads remain available.

## Find the tools

Open Studio and a project. The settings icon in the editor opens **Creator tools**. Text also contains My presets, Brand kit, Captions, Tutorial graphics and Save preset; Canvas contains Safe areas and Cover; Effects contains Before & after. All remain in the existing single-row nested menu.

1. **Saved text presets:** Select a text/title layer, choose Text → Save preset, name it. Reuse through Title library → My presets. Copies keep text, fonts, colors, placement and animation. Long-press a saved card to delete the library copy; existing timeline objects stay intact. Up to 100 saved designs.
2. **Brand kit:** Save your name, role, handle, font, accent, text/background colors and optional PNG/JPEG/WebP logo. Turn automatic brand use on for new library designs, or explicitly apply to selected text. Logos are copied to private storage at up to 1024px. Existing projects do not silently change when the kit changes.
3. **Animated captions:** Word highlight, karaoke, clean interview and bold-word styles. Apply to a selected text layer or all plain captions. Type/paste text to generate editable five-word cues over a chosen span, or import UTF-8 SRT at the playhead. SRT cue times are preserved; word timing inside cues is evenly estimated. This is not automatic speech transcription. Correct words and cue timing with the normal text/timeline tools.
4. **Tutorial graphics:** Twelve vector presets: arrow, focus circle/rectangle, three numbered steps, keyboard shortcut, tap rings, detail frame, tip, warning and checklist. Edit words, font and colors; move/scale/rotate/keyframe through the existing text tools. They join the existing 76 designs for 88 total.
5. **Starter projects:** Tutorial, product review, interview, event highlight and presentation. Each creates a separate 40-second draft with five editable section headings and an outro CTA. Add your footage to the video track and adjust titles/timing. These are reusable outlines, without bundled stock footage or music. Your previous draft is retained.
6. **Before & after:** Add after footage as a video overlay above before footage. Choose the overlay, then vertical/horizontal divider or animated reveal, and apply. Drag the divider directly on the preview when the comparison overlay is selected (use Transform to move the entire overlay). Divider position previews live; reveal duration is selectable. Side-by-side reduces the two views to half scale. Match footage timing yourself; undo restores previous geometry.
7. **Music ducking:** Mark voice tracks Narration and music tracks Duck. Use Analyze main camera speech to include camera audio. Adjust reduction and fade; optionally enable Duck on all non-narration audio. Detection uses audio activity, not a neural speech classifier; loud non-speech sounds can trigger it. Camera activity is mapped through clip trim, speed and timeline placement. Preview and export share the envelope. Analysis runs only while the editor is active, supports cancel and stops at high thermal status.
8. **Safe-area guides:** TikTok, Reels, Shorts and a presentation margin. Conservative approximate preview guides; platform layouts vary. They never enter video exports or captured covers.
9. **Cover designer:** Seek to a frame, choose Cover, add a headline, select font/color/ratio, darken the background and include your brand logo. Drag the headline and pinch to resize. Save a separate JPEG to Pictures/SoloCamera/Covers. Sizes: 1280×720, 1080×1920 or 1080×1080. The saved cover becomes the project-library image. Timeline pixels come from the actual composed preview.
10. **Favorites and recents:** Star a library card. Favorites, Recent and My presets are beside All in the title library. Recent records the last 20 used designs.

## Validation

Android build, test APK and lint; connected-device tests use isolated projects and synthetic red/blue video. Coverage includes persistent copies, brand identity, saved/favorite/recent indexes, all starter outlines, tutorial graphics, SRT cue timing, caption animation, camera ducking, comparison pixels in a real MP4, caption MP4, gallery JPEG MIME/header/dimensions, and the saved-library UI. Captured images live under tests/creator-suite-qa.

## v0.50 placement and keyframes

Preset entrance/exit choreography is now stored separately from manual keyframes. Moving or resizing a fresh preset sets its placement for its full duration. Recognized old generated keys are migrated out of the manual timeline; additional user keys are retained. Existing animated presets with user keys move as a whole unless manual transform keyframing has explicitly been enabled.

Tap a timeline diamond to highlight it, then use the trash icon beside the preview keyframe button or Keyframes → Delete selected. Remove all clears only the selected layer’s manual keys, keeps its current pose and preserves the preset’s entrance/exit animation. Undo/redo applies. Deleting the final keyframe preserves the current position, scale, rotation and opacity, including after reopening the project.

To author your own motion, add/select a keyframe and use Animate transforms in the Keyframes panel. Move the playhead and transform the object to add another key. Turn that option off to reposition an entire preset without adding keys. Still in the title designer controls the preset’s authored animation independently of your manual keys.

## v0.51 — Profession motion pack

- 50 original vector title designs across ten professions; catalog total 138.
- Profession selector, editable text/fonts/colors, neon glow and particle controls.
- Bounded animated sparkle/sheens and staggered component entrances/exits, shared by preview and export.
- Optional original synthesized title accents, volume slider and explicit sound preview. Title-linked audio follows moves, trims, mute and deletion, including MP4 and audio-only export.
- No model or asset download. Sound cache is created only for used/previewed designs.
- See PROFESSION_PRESETS.md for instructions.

## v0.51.1 — Editor playback repair

Fixed preview startup on interleaved screen recordings/high-bitrate media: the player now prioritizes minimum playable buffer duration over its byte target. Previously the byte target could be reached before audio/video were playable; loading stopped while the editor waited for READY, leaving the playhead permanently still.

Preset sound accents no longer participate in the video readiness barrier. They follow the editing clock, avoid seek corrections while buffering, throttle drift correction and release their players when paused. A slow or unavailable accent cannot freeze the timeline.

Reproduced on the connected TECNO with a copy of the affected 266.9-second screen recording project: v0.51 stayed BUFFERING at time zero, buffered duration zero and loading false. The fixed build advances video/playhead from zero and after seeks to 650 ms and 12 seconds. Original project/media files are retained.

## v0.52 — Privacy blackout

Added Edit / Effects → Blackout. Draw a rectangle over the paused video in a fullscreen view, then apply. It becomes an opaque privacy object on its own image track, with resize, positioning, independent timing, duplication, undo/redo and manual keyframes. Covers render above ordinary graphics and stay opaque even with opacity keyframes. No external assets, AI models or network service. See PRIVACY_BLACKOUT.md.

- v0.52.1: repaired export buffering and animated-title bitmap lifetime. The current 9-clip project exported at 1080 × 2400 with video and audio verified on TECNO KM7k; blackout drawing and MP4 pixel checks passed.
