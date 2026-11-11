# Creator manual

The illustrated user manual is generated from `tools/manual_content.py`, `tools/manual_screen_updates.py` and `tools/manual_recent_updates.py` by `tools/build_user_manual.py`. The curated app screenshots used by the PDF are in `docs/manual/screens`. The last verified build was Android **0.52.1 (build 58)**: **98 pages**, 92 chapters, 40 screenshot assets and no layout overflow. It covers camera/monitor pairing, Windows webcam and OBS screen demo, screen recording, Studio editing, privacy blackout, export and the present limitations of Publish Studio.

To regenerate on Windows, install Python with `reportlab` and Pillow, then run:

```powershell
python tools/build_user_manual.py
```

The PDF is written to `output/pdf/Solo-Camera-Complete-User-Manual-v0.52.1.pdf`. `output/` is ignored by Git because it is generated. Verify the page count and render pages after changing any text or screenshot. The 0.52.1 layout check is recorded in `docs/manual/validation-v0.52.1.json`.

Screenshots show sample app states, not creator media. If you capture a new screen, remove account identifiers, QR secrets and personal project thumbnails before committing it. Keep instructions version-specific: app labels and supported camera controls can change.
