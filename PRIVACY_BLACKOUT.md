# Privacy blackout — v0.52

In the editor, choose **Edit → Blackout** (also under Effects). The paused video opens full screen. Drag a rectangle over a phone number, contact name, address, notification or other private detail. Draw again if needed, then tap **Apply blackout**.

The opaque black cover starts at the playhead and runs to the project end. It has its own image timeline item labelled Privacy blackout. Trim or move that item to change the covered time interval. Drag the cover in the normal preview to move it, pinch or use Cover width / Cover height to resize it. Choose Add another blackout for more areas. Whole video extends the selected cover across the project; End here stops it at the current playhead.

The dedicated Privacy toolbar avoids text-editing and photo-background tools. Undo/redo, duplication, removal and manual motion keyframes use the existing editor operations. If the screen scrolls or the private detail moves, set position keyframes to move the cover with it. Tracking is manual for this tool.

Covers are generated as fully opaque black pixels and rendered above ordinary text, images and video overlays in preview and export. Opacity keyframes do not make a privacy cover transparent. The cover is present only within its trimmed timeline interval. No downloaded model, network access or additional media asset is required. Original source media remains unchanged; the exported video contains the cover.
