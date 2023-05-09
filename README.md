# Timezone Updater

Copyright (C) 2015 Anton Skshidlevsky, GPLv3

This app downloads and updates time zones to the latest version on your device. This update should fix all known problems with time zones, such as incorrect time in Android and some applications.

The app is available for download on Google Play and GitHub:

<a href="https://play.google.com/store/apps/details?id=ru.meefik.tzupdater"><img src="https://gist.githubusercontent.com/meefik/54a54afa7cc1dc600bdb855cb7895a4a/raw/ad617c006a1ac28d067c9a87cec60199ca8fef7c/get-it-on-google-play.png" alt="Get it on Google Play"></a>
<a href="https://github.com/meefik/tzupdater/releases/latest"><img src="https://gist.githubusercontent.com/meefik/54a54afa7cc1dc600bdb855cb7895a4a/raw/ad617c006a1ac28d067c9a87cec60199ca8fef7c/get-apk-from-github.png" alt="Get it on Github"></a>

**Requirements**:

- Android 4.0 (API 14) or later
- Superuser permissions (root)
- [BusyBox](https://github.com/meefik/busybox)

**Update procedure**:

1. Get superuser privileges (root).
2. Install BusyBox.
3. Check your connection to the Internet.
4. Tap UPDATE button.
5. Restart your device.

The following files will be updated:

- [TZ data](https://www.iana.org/time-zones/): /data/misc/zoneinfo/tzdata or /system/usr/share/zoneinfo/*
- [ICU data](https://icu.unicode.org/): /system/usr/icu/*.dat

**Attention!** Before starting the update procedure is recommended to make backup copies of these files or the entire system.
