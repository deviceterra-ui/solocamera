# Play Console app-content review

Use this as a review aid, not an automatic answer key. Confirm the final signed AAB, its merged manifest, all bundled SDK behavior and every Play Console question before certifying responses.

## Data and privacy

- The app records camera video, microphone audio and optionally the screen after user action. Local projects, scripts, drafts and exports reside on the device. Media selected through Android's document picker is accessed only to edit or export it.
- Approved phone-to-phone and phone-to-Windows sessions send preview content and controls over the local network. This is transfer to a user-chosen peer, and Play's exact Data safety wording must be evaluated against that flow.
- The current release has no configured publishing service. No automatic social publishing or Solo Camera account is available. Do not declare a live account workflow based on placeholder UI.
- Optional model and font downloads contact Google Cloud Storage or GitHub over HTTPS. Recheck host and SDK data practices before choosing “no data collected.”
- The app declares no advertising ID permission and has no ad SDK in the current source. Verify the merged manifest and runtime dependencies before answering the Ads and Data safety sections.
- Publish a final privacy policy with a confirmed public contact address. Check it from an unsigned-out browser and from the link in the installed app.

## Permissions and app access

- **Camera / microphone:** direct capture, presenter bubble, audio recording and optional live audio. Explain these user-initiated uses if Google requests a declaration.
- **MediaProjection foreground service:** screen recording and live screen-demo streaming. The user initiates capture through Android's consent dialog and sees a foreground notification. Record a short demonstration if Play requires foreground-service evidence.
- **Camera and microphone foreground service types:** used by the same capture service while presenter and audio capture are enabled. Confirm the merged manifest types match runtime use.
- **Display over other apps:** movable presenter bubble and screen annotations. Explain the creator-demo purpose and verify that users can dismiss these overlays.
- **Notifications:** capture status and controls. Check behavior on Android 13+ when notification permission is declined.
- No `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, or `MANAGE_EXTERNAL_STORAGE` permission is declared in the source manifest; imports use Android document pickers. Verify the merged manifest for transitive additions.
- App access: the core camera/editor can be used without signing in. State this accurately in Play Console; provide reviewer instructions for a two-phone monitor demonstration if requested.

## Quality before production

- Install the *Play-delivered* internal-test build on two phones, not just the local debug APK. Verify QR pairing, remote camera changes, recorded audio/video sync, screen capture, Studio timeline and export.
- Confirm 9:16 and 16:9 exports, repeated playback, overlay editing and privacy blackout in the final MP4.
- Test permission denial, app backgrounding and thermal behavior during a long recording. A local build and lint pass do not establish device quality.
- Supply authentic phone screenshots from the release build. Existing manual screenshots document an earlier build and need review before use in a public listing.
- If this personal developer account was created after 13 November 2023, Google requires at least 12 continuously opted-in closed testers for 14 days and an application for production access. Check the account's actual eligibility in its dashboard.
