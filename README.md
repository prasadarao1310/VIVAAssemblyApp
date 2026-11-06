# VIVA Assembly Companion — 0.2.0

Android app for VIVA The School, Nambur, Guntur.

## Install / update
Download **VIVA-Assembly-Android-APK** from a successful **Build VIVA Assembly APK** Actions run. Extract the ZIP and install **VIVA-Assembly-0.2.0.apk**. Android 8.0+; package `in.viva.assembly`. Updates keep local drafts, affirmations and the selected logo when signed with the cached CI key.

## Requested editorial policy

| Category | Allowed publishers |
|---|---|
| International | The Indian Express; The Hindu |
| National | The Indian Express; The Hindu |
| Indian sports internationally | The Indian Express; The Hindu |
| Andhra Pradesh | ఈనాడు (eenadu.net); ఆంధ్రజ్యోతి (andhrajyothy.com); సాక్షి (sakshi.com) |

**The publication date must be exactly the assembly date minus one, in Asia/Kolkata.** For a 5 November assembly, only publications from 4 November, 00:00–23:59 IST, qualify. Neither older reports, today's reports, nor today's match fixtures are substituted. Month/year/leap-day boundaries are handled with calendar arithmetic. Future publication timestamps are rejected.

Google News RSS is used for discovery, with publisher-restricted queries. Every result is then checked against the publisher-domain allowlist and exact IST date; the query alone is not trusted. Names such as “The Hindu” do not pass without matching domain provenance. Google News links must carry approved publisher-domain metadata. Direct manual links must belong to an allowed publisher.

Feed dates are metadata and may reflect an updated article. **A teacher must open the original article, verify its original publication date, check facts/context, and confirm the category before assembly.** This is not independent fact-checking or a guarantee that every story is accurate. Edit the source date if needed; the app enforces the entered publication date. Paywalled content is not bypassed.

There are two editable slots per category. Missing eligible stories remain empty: no invented story, other publisher, or older-day fallback. Manual entries use the same publisher/date checks. Unchanged saved entries that fail the new rules are cleared on refresh. Edits made during a refresh are preserved but cannot be exported until corrected. Export independently revalidates all eight entries, even if the review checkbox is already ticked. Exact normalized duplicates are rejected.

Andhra Pradesh uses the Telugu Google News edition and keeps the original Telugu headline text. Teachers can rewrite it in English after reading the article; no unverified automatic translation is generated. Android text shaping is used in the PDF for Telugu.

## Importance of the day

International and Indian significance remain editable. Verified entries in the limited starter calendar now include 100-plus-word educational notes. These distinguish an observance's name/date from suggested classroom discussion, and do not invent annual themes or unsupported historical details.

**The combined Importance of the Day section must contain at least 100 words.** A live word count is shown, and PDF export is blocked below that threshold. Known entries generally exceed 100 words individually. An unknown entry is explicitly marked unverified; if neither calendar has an entry, a teacher must add a sourced note to reach the minimum. Missing dates are not filled with invented celebrations.

The reference PDF was used for arrangement, not as a factual source. In particular, Ayurveda Day is not labelled as 5 November; the Ministry of Ayush designated 23 September from 2025 onwards.

## A4 layout

Single column, matching the supplied example's sequence:
1. Morning news heading with the exact previous-day date.
2. International — two items.
3. National — two items.
4. Andhra Pradesh — two items.
5. Indian sports internationally — two items.
6. Namburu weather.
7. A new A4 page: Importance of the Day (international, then national).
8. Thought for the Day.
9. Monthly affirmation.
10. Word for the Day, with part of speech, pronunciation, meaning and usage.

The VIVA logo keeps its proportions. Select the supplied original logo once under **A4 export**. Standard content targets two A4 pages; long edits continue to additional A4 pages without lost text. The PDF uses Android StaticLayout for text shaping and line wrapping. This is a genuine PDF export, not a screenshot.

## Daily use
Choose the assembly date → Refresh → inspect publisher/date and original article → edit the eight entries → review significance, thought and word → enter/import monthly affirmation (.txt) → select school logo → preview/save A4 PDF. The weather is for the assembly date, while news is for the previous date.

Affirmations are stored separately by year/month. Word and original thought banks rotate every 31 days. Uploaded logo, existing affirmations and custom edits are preserved across this update. Old default observance labels are upgraded to notes; custom text is not overwritten. Old review flags are reset after migration.

Weather: Open-Meteo, approximate Nambur coordinates 16.36 N / 80.53 E, daily conditions, high/low and maximum rain probability. It is a model forecast, not an on-campus observation. Forecasts are available for today through the next 15 days. Source attribution: Open-Meteo, CC BY 4.0; free API intended for non-commercial use.

## Privacy
No login, ads, AI key or analytics SDK. Drafts and branding stay in private app storage; Android backup is disabled. Refresh sends news queries/dates to Google News and fixed village coordinates to Open-Meteo. The affirmation, logo and edited sheet are not uploaded. Uninstalling deletes private drafts.

## Build and validation
JDK 17, Gradle 8.11.1, AGP 8.9.2, SDK 35 / Build Tools 35.0.0.
```sh
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```
Use installed Gradle or generate a wrapper with `gradle wrapper --gradle-version 8.11.1`.

CI runs unit tests and Android lint, performs an informational live-feed/weather check, builds the APK, verifies the signature and uploads metadata/checksums. Feed availability varies; a successful build does not guarantee two stories for every category/date. Debug signing key is cached; losing that cache can require reinstalling, which deletes local data.

Regression tests cover:
- Exact previous-day IST start/end, adjacent-day rejection, future dates, year and leap-day boundaries.
- Allowed publishers by category, spoofed domains, misleading names and missing provenance.
- Missing dates, malicious XML, unsafe links and duplicate headlines.
- 100-word threshold, verified note lengths, unverified-day handling and vocabulary fields.
- Text wrapping and Unicode preservation.

Physical Samsung installation, updated PDF visual appearance, Telugu font rendering, printer margins and on-device networking still require device acceptance testing.

Official calendar / provider references:
- https://www.un.org/en/observances/list-days-weeks
- https://knowindia.india.gov.in/national-identity-elements/national-days.php
- https://www.ursc.gov.in/nsd.jsp
- https://www.pib.gov.in/Pressreleaseshare.aspx?PRID=2072203
- https://ayush.gov.in/resources/pdf/PressRelease/Ayurveda_Day_to_Be_Celebrated_on_23rd_September_Every_Year.pdf
- https://open-meteo.com/en/docs
