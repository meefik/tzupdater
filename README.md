# tzupdater

Copyright (C) 2015-2017 Anton Skshidlevsky, [GPLv3](http://opensource.org/licenses/gpl-3.0.html)

This app downloads and updates a time zones to latest version on your device. This update should fix all known problems with time zones, such as incorrect time in Android and some applications. Updated the following files:

* /data/misc/zoneinfo/tzdata or /system/usr/share/zoneinfo/*
* /system/usr/icu/*.dat

Before starting the update procedure is recommended to make backup copies of these files or the entire system.

**Requirements**:

* Android 2.3 (API 9) or later
* Superuser permissions (root)
* [BusyBox](https://github.com/meefik/busybox)

**Update procedure**:

1. Get superuser privileges (root).
2. Install BusyBox.
3. Check the connection to Internet.
4. Tap UPDATE button.
5. Restart your device.

**References**:

* [Source code](https://github.com/meefik/tzupdater)
* [Releases](https://github.com/meefik/tzupdater/releases)
* [Donations](http://meefik.github.io/donate/)
