# Days of Love v2 💗

A romantic Android relationship counter and widget.

## What's new in v2

- Custom partner names
- Relationship date editor
- Polished romantic dashboard
- Approved heart/calendar launcher icon
- Widget theme presets
- Custom widget background and text colors
- Adjustable widget background strength from fully transparent text-only to solid
- Responsive widget layout with a "Together since" line
- Master notification switch
- Optional persistent day-counter notification
- Anniversary reminders at configurable lead times, including an optional daily final-week countdown
- Monthsary reminders
- Day-milestone celebrations
- Optional gift-plan reminders
- Configurable reminder time
- Next anniversary, next monthsary and next milestone cards
- Shareable day-count text
- Local-only relationship data, no account required

## Free phone-only build

Push this repository to GitHub. The included GitHub Actions workflow builds an installable Android debug APK.

1. Open the repository's **Actions** tab.
2. Open **Build Android APK**.
3. Wait for a green check.
4. Open the run and download the **DaysOfLove-v2-APK** artifact.
5. Extract it and install `app-debug.apk` on Android.

## Important beta signing note

v2 includes a fixed **test-only debug keystore**. This means future v2 beta builds from this project can update each other normally.

The original v1 APK was signed with GitHub's temporary debug key, so Android may refuse to install v2 directly over v1. If that happens, uninstall the old Days of Love app, install v2, and re-enter the relationship date. This test keystore must **not** be used for a Play Store production release.

## Play Store later

Before production release, use a production application ID, a protected release signing setup / Play App Signing, final privacy text, store screenshots, and an Android App Bundle (`.aab`).
