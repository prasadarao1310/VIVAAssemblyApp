# VIVA Assembly Companion

Native Android assembly preparation app for **VIVA The School, Nambur, Guntur**.

## Download

Open **Actions → Build VIVA Assembly APK**, select a successful run, download **VIVA-Assembly-Android-APK**, extract the ZIP, and install **VIVA-Assembly-0.1.0.apk**. Requires Android 8.0+ (API 26). This is a debug APK for school evaluation, not a Play Store release.

## First use

1. Choose the assembly date (India time).
2. Tap **Refresh news & weather**. It retrieves suggestions for 2 international, 2 national, 2 Andhra Pradesh and 2 Indian sports international headlines.
3. Read the linked sources and edit the wording and categories for your students. **Choose** offers alternatives from the fetched feed; **Source** edits publication details or opens the article.
4. On **Assembly**, review weather and observances, enter the month's affirmation or import a UTF-8 **.txt** file, and edit the word/meaning/example and thought.
5. On **A4 export**, select the original VIVA logo image supplied by the school. It is copied into private app storage and reused; its proportions are preserved. The attachment could not be bundled from the chat environment, so this one-time selection is required.
6. Review the content, tick the review checkbox, then **Preview & save PDF**. Choose a folder through Android's document picker. Open the saved PDF in My Files to print/share it.

Typical short content fits one two-column A4 sheet (595 × 842 PDF points). Longer entries continue on additional A4 pages, with no silent clipping. Preview includes every page. If you need exactly one page, shorten the editable entries after checking the preview. Unreviewed exports say DRAFT.

## Included

- Eight editable headline slots with publisher, article link and publication time.
- Online retrieval from Google News RSS, using category queries and a bounded date window.
- Source freshness filtering, exact normalized headline deduplication and a small sensitive-topic exclusion filter.
- Nambur forecast from Open-Meteo, approximate coordinates 16.36 N / 80.53 E. Daily high/low, conditions and maximum rain probability.
- Limited fixed-date starter calendar: 30 international and 10 Indian observances, with official source links.
- A different word, definition/example and original thought each day from a 31-day rotating bank.
- Monthly affirmation, keyed by year and month, typed or imported from a text file.
- Date-specific local drafts, review state, logo import, A4 PDF preview and export.
- Offline editing/export of saved drafts. Network needed only for refresh and opening links.
- Unit tests for RSS parsing, unsafe XML/URLs, date boundaries, duplicates and text wrapping.

## Editorial limitations

This app fetches headlines; it does **not** independently fact-check articles or generate AI summaries. Read and rewrite headlines before presenting them. RSS results may be off-topic, and a keyword filter cannot guarantee school suitability or that a sports story involves Indian participation internationally.

The date window is the selected day plus the two preceding calendar days, bounded by the present time. Future assembly dates use currently available news and display a warning. Old assemblies may have no retrievable feed results. Fewer than two suitable items leave slots empty or retain existing edited content with an explicit status; no replacement news is fabricated. Publication dates remain visible. Failed refreshes preserve existing drafts.

The observance calendar is deliberately incomplete and does not contain movable dates or all national days. “No entry” means no entry in this starter calendar, not that no observance exists. Use official sources and edit the field when needed. Annual themes are not invented. The default thoughts are original lines, not quotations attributed to famous people.

The weather endpoint supports today through the next 15 days. Older dates need a teacher-supplied report or a previously saved forecast. It is a model forecast for the vicinity, not an on-campus station reading. Weather data attribution: **Open-Meteo, CC BY 4.0**. The free endpoint is for non-commercial use; review provider terms before any commercial distribution.

## Privacy and storage

No login, advertising SDK, AI key or analytics SDK. Drafts, affirmations and selected logo stay in the app's private storage. Android backup is disabled. Refresh sends category/date queries to Google News and fixed approximate village coordinates to Open-Meteo; it does not upload the affirmation, logo or edited sheet. PDFs are saved only to the destination selected by the user. Uninstalling removes private drafts.

## Build

JDK 17, Gradle 8.11.1, Android Gradle Plugin 8.9.2, Android SDK 35 / Build Tools 35.0.0.

```sh
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

No Gradle wrapper binary is bundled in this initial source tree. Use installed Gradle 8.11.1 or generate a wrapper with `gradle wrapper --gradle-version 8.11.1`. Output: `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions tests, lints, builds, verifies the APK signature and uploads APK metadata/checksum alongside the APK. It caches the debug signing key for update installs. If that cache is lost, a subsequent debug build may require uninstalling the old build; export needed sheets first.

## Device acceptance checks — not yet performed on a physical Samsung

- Install; check every tab and date picker.
- Fetch online; confirm all category assignments and timestamps against source articles.
- Edit during refresh; confirm changed text is preserved.
- Disconnect internet; confirm errors do not erase existing text.
- Change months; confirm affirmations remain attached to the correct month.
- Import the supplied VIVA logo and a UTF-8 affirmation file.
- Export a normal sheet and an intentionally long sheet; inspect all A4 pages.
- Rotate the phone while the document picker is open; check PDF save.
- Test dates outside weather range and unavailable observance entries.
- Inspect font rendering, logo clarity and print margins on the Samsung device/printer.

Sources / service documentation:
- https://www.un.org/en/observances/list-days-weeks
- https://knowindia.india.gov.in/national-identity-elements/national-days.php
- https://www.ursc.gov.in/nsd.jsp
- https://www.pib.gov.in/Pressreleaseshare.aspx?PRID=2072203
- https://open-meteo.com/en/docs
- https://open-meteo.com/en/terms
- https://news.google.com/
