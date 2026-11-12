# Google Play release checklist

The Android package is `studio.solocamera`. Release build 59 is version 0.52.2, targets API 36, and is produced with `./gradlew :app:bundleRelease` using JDK 21. The signed bundle is `app/build/outputs/bundle/release/app-release.aab`.

The upload key is created once by `tools/create-upload-key.ps1`. Back up both `release/solocamera-upload.p12` and `release/upload-signing.properties` in a secure location before submitting the first bundle. These files are ignored by Git and must never be uploaded to the repository. Google Play App Signing should manage the distribution key; this local key is the upload key. Do not regenerate the upload key for future releases.

## Account and submission

1. Sign into the existing **Donaldigor** personal developer account using `madeinbrooklynbro@gmail.com`. Do not create a new developer account. Its account dashboard currently has other apps in internal testing. The owner must handle any outstanding identity, contact, or trader-status declarations that Google requires.
2. Create an app named **Solo Camera**, default language English, category **Video Players & Editors** or **Photography** as appropriate to the final listing, free unless the owner chooses otherwise. Use the existing package `studio.solocamera`. Google confirmed the package name was available on 12 November 2026. The owner must personally review the Developer Program Policies and US export-law certifications on the final creation form.
3. Complete the store listing using `listing.md`, the actual device screenshots, a 512×512 icon, and a 1024×500 feature graphic. Do not represent planned social publishing or AI as shipped functionality.
4. Publish the app-specific privacy policy at a public, non-editable HTTPS page and link it in both the listing and the app. The support address is `madeinbrooklynbro+solocamera@gmail.com`, a Gmail plus-address that delivers to the owner's existing inbox. It is not a separate Google account.
5. Complete the App content declarations (Data safety, permissions, ads, target audience, content rating, app access, and any other prompts) against the *final* binary and actual service configuration. The questionnaire cannot be truthfully preselected from source code alone.
6. Upload the signed AAB to internal testing first. Test camera, recording, QR pairing, screen capture, Studio import/export, privacy blackout, and the privacy-policy link on real devices. For new personal developer accounts, satisfy Google's closed-test and production-access requirements before public launch.
7. Submit the production release for Google review. Production availability begins only after Google accepts the app and the account's production access is enabled.

The current app offers local drafts in Publish Studio; automatic posting and account connection are not active in the production build. The store listing must say so.
