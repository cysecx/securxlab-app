# Securxlab app (Android tablet / phone / Fire TV)

A thin app that opens your live dashboard full screen. It has no dashboard code inside, so when you update
the website the app updates too. You never rebuild it for dashboard changes.

## Build it for free (no Android Studio needed)
1. Create a free GitHub account, make a new **private** repository, and upload everything in this folder
   (keep the `.github` folder). Easiest: "Add file > Upload files" and drag the whole folder in.
2. Open the repository's **Actions** tab, pick **Build Securxlab app**, click **Run workflow**.
3. When it finishes (about 3 minutes) open the run and download the **Securxlab-apk** file. Unzip it to get `app-debug.apk`.

To use another address, edit `DASH_URL` in `gradle.properties` before step 1.

## Install on a Fire TV / Fire Stick
1. Fire TV: Settings > My Fire TV > Developer options > turn on "Install unknown apps" for **Downloader**
   (install Downloader from the Amazon Appstore, it is free).
2. Put `app-debug.apk` somewhere with a direct link (for example a GitHub release asset, or a file on your NAS web server)
   and open that link in Downloader, then Install. (Or send it with the free "Send files to TV" app.)
3. Open Securxlab from Your Apps & Channels. Use the remote's arrows to move, Select to press, Back to go back.

## Install on an Android tablet or phone
Open the downloaded `app-debug.apk` and allow "install from this source" when asked. Or skip the app on Android and
use Chrome > menu > **Install app** (works the same, no build needed).
