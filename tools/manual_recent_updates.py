"""Creator-suite and privacy workflows added after the Android 0.45 manual."""

add('Studio essentials','Start from the current Studio home','50-studio-new-project',r'''
Open **Studio** from the Solo Camera home screen. The large **+ New project** button starts an empty edit, so you can add video, photos, audio, titles and effects when you need them. Choose **+ Starter projects** when you want a prepared tutorial, review, interview, event or presentation outline instead.

**Creator templates** opens reusable editing setups. **Import media** brings source files into Studio, while **Library** opens the media browser. Saved projects appear below the buttons as swipeable cards; choose Edit on the project you want to resume. Recordings and finished exports are organized in their own groups farther down the screen.

N|Creating an empty project does not delete or replace an older project. The screenshot stops above the project cards to keep the owner's media private.
''','Current 0.52.1 Studio home captured on TECNO KM7k. The image is cropped before personal project thumbnails.')

add('Creator tools','Open the new creator workspace',None,r'''
Open Studio, then open or create a project. Tap the **settings icon** in the editor header to open **Creator tools**. This menu gathers the title library, brand kit, captions, profession presets, tutorial graphics, starter projects, comparison, safe-area guides, cover designer, voice-over, take markers, ducking, microphone sync and reusable templates.

The same tools also appear in their relevant nested editor menus. For example, Text contains title designs and captions, while Canvas contains safe-area guides and Cover. Use the menu's back arrow to return to its parent rather than trying to keep two rows of controls open.

N|Creator tools run locally. The title designs in this chapter do not require a model, internet connection or subscription. Optional AI downloads remain a separate choice; a diamond label is not a requirement for these built-in designs.
''')

add('Creator tools','Save a text design and build a brand kit',None,r'''
H|Save and reuse a design

Select a text or title item on the timeline. Choose **Text > Save preset** or **Creator tools > Save selected text preset**, give it a name and save. Reuse it from **Title library > My presets**. The copy keeps its font, colors, placement and animation. Hold a saved library card to delete that reusable copy. Existing items in projects remain. You can keep up to 100 saved designs.

H|Set your brand once

Open **Creator tools > My brand kit**. Enter a name, role and handle; choose a font and accent, text and background colors; optionally import a PNG, JPEG or WebP logo. Turn on automatic brand use for new library designs, or apply the kit to the selected text. Review the preview before adding the title. Changing the kit later does not silently alter older projects.

N|Keep a separate copy of your logo and original font files. The app stores a working copy for its own projects; brand settings do not replace an external backup.
''')

add('Creator tools','Make animated captions and tutorial graphics',None,r'''
H|Editable captions

Open **Creator tools > Animated captions** or the Text captions menu. Pick Word highlight, Karaoke, Clean interview or Bold word. Type or paste words to make editable five-word cues across your chosen time span. Alternatively import a UTF-8 SRT subtitle file at the playhead. Its cue start and end times are retained; timing of individual words within each cue is estimated evenly.

Play the section, select any caption on its text row and correct its words, duration, color or position using the normal text and timeline tools. Apply a style to one selected plain caption or to all plain captions as needed.

H|Show what you are explaining

Open **Tutorial graphics** in Creator tools or Text. Choose an arrow, focus circle or rectangle, numbered step, keyboard shortcut, tap rings, detail frame, tip, warning or checklist. Edit its words and colors, then move, scale, rotate or keyframe it like another text object. The graphic is a timeline layer and can be trimmed or deleted independently.

N|Caption generation here is based on text you provide. It is not speech-to-text transcription; check the wording and timing yourself.
''')

add('Creator tools','Start from an outline and compare two clips',None,r'''
H|Starter projects

Choose **Creator tools > Starter projects**, then pick Tutorial, Product review, Interview, Event highlight or Presentation. Each starts a separate 40-second draft with editable section headings and an outro call to action. Add your own camera footage to the video row, then adjust the section timings and titles. The starter contains no stock footage or music; your previous draft stays available in the project library.

H|Before and after

Put the before clip on the main video row and the after clip above it as an overlay. Select the overlay and open **Creator tools > Before & after**. Pick a vertical divider, horizontal divider or animated reveal, then apply. Drag the divider directly in the preview while that overlay is selected. Use Transform to move the entire overlay. Preview the result across the edit and align both clips in time.

N|Side-by-side comparison reduces the two views to half-scale. The divider and reveal belong to the selected comparison overlay; changing another row will not retime the comparison for you.
''')

add('Creator tools','Use safe guides and save a cover',None,r'''
H|Keep titles clear of social controls

Choose **Canvas > Safe areas** or **Creator tools > Social safe-area guides**. Pick TikTok, Reels, Shorts or presentation margins. Place important words and faces inside the guide while editing. The guide is only an on-screen aid; it does not appear in the video export or saved cover. Check the target platform before publishing because its interface may change.

H|Create a project cover

Seek to a useful frame. Open **Canvas > Cover** or **Creator tools > Cover & thumbnail**. Add a headline, choose its font and color, pick 16:9, 9:16 or square, darken the background if needed and include your brand logo. Drag the headline; pinch to resize it. Save the JPEG to **Pictures/SoloCamera/Covers**. The saved image becomes the project's library thumbnail. It is separate from the MP4 export.

Use **Favorites** and **Recent** in the title library to return to designs you use often. Recent keeps the last 20 used designs.
''')

add('Title presets','Choose a profession title and sound','47-profession-presets',r'''
Open **Text > Profession presets** or **Creator tools > Profession presets**. Open Professions and choose Technology, Agriculture, Medicine, Education, Business, Finance, Food & hospitality, Beauty & fitness, Construction or Creative media. Each has five designs, for 50 new designs and 138 built-in title designs in total.

Tap a design. Edit its headline, subtitle, label, font and colors while watching the preview. Set duration and entrance/exit animation; adjust the glow slider and particle switch. Tap **Preview animation + sound** to hear the short entrance accent. Switch synchronized entrance sound off, or lower its volume, if you want silence. Tap **Add to timeline**.

The design appears as editable text on its own row. Move or resize it in the preview; trim or move it on the timeline. Select it and reopen Text to change sound, volume, particles or glow; **Edit design text & colors** opens the full designer. Its accent follows the title's timing and disappears with the title. No separate sound row is required.

N|The pictograms, motion and accents are built into the app. The sound is generated and cached only when used or previewed. These designs do not download an AI model.
''','Actual profession-title library filter from the current app. Example thumbnails are app-rendered title designs.')

add('Title presets','Move a preset without accidental motion',None,r'''
Select the title on its text row. To change where it sits for the whole shot, drag or pinch it in the preview while **Animate transforms** is off. Its authored entrance and exit still run, but your new placement remains the base position through the interval. Trim or move its timeline item as needed, then play from before the entrance to verify the result.

To create deliberate motion, select a keyframe or add one with the diamond under the preview. Turn on **Keyframes > Animate transforms**. Move the playhead, change the object's position, scale, rotation or opacity and add the next keyframe. Use easing controls to shape the travel between keys. Turn Animate transforms off before repositioning the whole design again.

To remove a key, tap its diamond on the timeline to highlight it. Use the trash icon beside the preview keyframe control or **Keyframes > Delete selected**. **Remove all** clears only the selected layer's manual keys while keeping its current pose and the preset's entrance/exit. Undo and redo work on these changes.

N|An older project may contain generated placement keys from a previous build. The editor migrates recognized generated keys while keeping user-created keys. Inspect the result once after opening an older project and save a backup before major changes.
''')

add('Privacy and export','Black out a number or private detail',None,r'''
In the editor, pause on a frame where the private detail is visible. Choose **Edit > Blackout** (also under Effects). The video opens full screen. Drag a rectangle around the phone number, address, name or notification, then tap **Apply blackout**. Draw again for another area.

The new **Privacy blackout** item appears on its own image row. It starts at the playhead and lasts to the project end. Trim or move that item to control when the cover exists. Choose **Whole video** to extend it across the project, or **End here** at a chosen playhead position to stop it. Drag its cover in the preview or pinch it to reposition and resize; Cover width and Cover height provide finer adjustment.

For a moving private detail, select the blackout and add manual position keyframes. Duplicate it for another detail. Undo, redo and delete work like other timeline objects. Preview every frame in the covered interval, especially at cuts, zooms and motion.

N|The cover is opaque black and is composited above ordinary graphics in preview and export. Opacity keyframes cannot reveal what is under it. Source footage remains unchanged, so share the exported video, not the unedited source, when privacy matters.
''')

add('Privacy and export','Finish and verify a complex export',None,r'''
Open **Export video** from the editor header. Choose the canvas and output quality, including 720p or 1080p, then start the MP4 export. Keep the editor open until completion. A long composition with many clips, animated titles and audio rows can take time; let the progress indicator finish before opening another app.

The 0.52.1 build repairs the earlier **7002 / No output sample written in 10,000 milliseconds** failure caused by buffering on complex projects. If you see this error on an older installation, update the app and retry from the saved project. If an updated export still fails, first confirm the source clips are still available, free storage is sufficient and each clip plays in the editor. Try a short range or 720p export to isolate the problem; keep the project and original media for diagnosis.

After export, open the MP4 from **Movies/SoloCamera/Exports** or the app's export library. Watch the beginning, a middle cut and the ending, and listen to narration and title accents. For privacy work, inspect every blackout during motion. A successful progress message alone is not a substitute for checking the saved file.

N|On a TECNO KM7k, the updated build completed a 9-clip project with animated titles and two voiceovers at 1080 x 2400, with video and audio tracks verified. Export speed and available resolution still depend on your phone and footage.
''')
