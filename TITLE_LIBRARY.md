# Titles, lower thirds and presentation slides — 0.46.0

Open **Studio → editing project → Text → Title library**. You can also open **Titles, lower thirds & presentation slides** in an empty project, or use **Creator tools → Titles, lower thirds & slides**.

## Library

40 curated presets: 12 lower thirds, 8 opening/chapter titles, 12 presentation slides, and 8 callouts. These use 12 underlying vector layout families with curated palettes, typography and sample copy. Browse visual cards by category; tap a card to see its animated customization preview.

Edit the headline/name, subtitle/role and, where applicable, section label. Pick a font and accent/background/text colors. Set duration from 3 to 30 seconds. Animation choices are Still, Soft rise & fade, Slide & fade, and Scale & fade. Editing a word, font or color resets the preview into a visible part of the animation so changes can be seen immediately.

**Add to timeline** inserts one editable Text object on its own track at the playhead. It does not overwrite the selected clip or automatically shift later videos. Full-frame slides cover the composition during their interval; place/trim them where needed. A project can consist of slides without imported video. Edit selected designs using **Edit design text & colors**. Standard text font, color, background, shadow and outline controls remain applicable. Move/scale/crop/trim/keyframe the complete grouped design using existing editor tools. Multiple title objects can be layered. Existing undo/redo and project persistence apply.

## Fonts

Six existing bundled professional families are available offline. **Download fonts** in text tools or **More fonts · optional downloads** in the designer opens nine additional opt-in fonts: Oswald, DM Serif Display, Manrope, Outfit, Barlow Condensed, Sora, Space Grotesk, Caveat and Anton. Font downloads are approximately 1.45 MB combined; individual exact sizes are shown before downloading. Internet is required only for the download. Cancel leaves the font uninstalled. Pinned HTTPS URLs, maximum bytes, exact size, SHA-256 and Typeface validation protect downloaded files. Installed fonts are available in both the designer and normal text font picker. TTF/OTF import is also available.

Fonts are sourced from google/fonts under their individual SIL Open Font Licenses. Catalog, commit-pinned URLs, checksums and sizes: app/src/main/assets/optional-fonts.json. Licenses are bundled in app/src/main/assets/licenses and viewable in the download prompt.

## Validation

Android build and lint passed. On TECNO KM7k: all 40 designs rendered at landscape and portrait sizes; styles survived JSON roundtrip; entrance/middle/exit opacity states passed. The visual library and customizer were exercised, headline editing applied, canceling Oswald left it uninstalled, and opt-in downloading installed/applied Oswald. A slides-only project exported to MP4 and a decoded frame contained the slide graphics. Preview and export use the same title renderer. Latest final preview refresh fix is covered by a repeat device check. These checks do not establish long-session performance or every language's font coverage. Design objects group headline/subtitle/decorations; individual decoration layers are not separately editable.

## Social CTA extension — 0.47.0

The library now contains **76 presets**, including **36 Social CTA presets** (three per platform). In an editing project open **Text → Title library → Social CTA**.

Platforms: YouTube, Facebook, Instagram, TikTok, LinkedIn, X, Threads, Twitch, WhatsApp, Telegram, Discord and Pinterest. Actions include Subscribe, Watch next, Like/comment/share, Follow, Save this post, Link in bio, Connect, Message us and Join our community/channel/server. Edit the CTA wording, handle/channel, label, font, colors, duration and animation before adding. These are editable video graphics, not interactive follow buttons or social-account connections.

The preset uses the platform's original Font Awesome 6.7.2 vector brand outline, uniformly scaled and rendered in white on a platform-colored badge. Brand outlines are not replaced with letters, emoji or approximate drawings. CTA palettes affect the card/text/decorations; platform badge colors remain separate. Assets are bundled and work offline with no download. Original SVGs, provenance and checksums are in assets/social-icons; upstream license/attribution is included. Icon assets are from Fonticons, Inc., CC BY 4.0. Platform trademarks remain with their respective owners.

The existing 40 preset IDs are preserved. New lower thirds and social cards use an inset default vertical position that remains inside landscape and portrait frames. Device checks cover all 76 presets in both orientations, persistence, animation, frame containment, font customization, actual timeline insertion and MP4 export. The exported test composition also checks for the YouTube badge's colored pixels; visual QA checks the recognizable platform marks.

## v0.48 component animation
All 76 library designs now reveal their background, accent/icon, eyebrow, headline and subtitle independently with staggered entrance and exit timing. The title designer, editor preview and MP4 export share the same cached component renderer. Choose Still in the design animation picker to disable animation. Position, scale and keyframes continue to transform the complete design. No models or extra downloads are required.

