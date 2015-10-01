# tzupdater

(c) 2015 Anton Skshidlevsky

Licensed under the [GPL version 3](http://www.gnu.org/licenses/) or later.

This app downloads and updates a time zones to latest version on your device. This update should fix all known problems with time zones, such as incorrect time in Android and some applications. Updated the following files:

* /data/misc/zoneinfo/tzdata or /system/usr/share/zoneinfo/*
* /system/usr/icu/*.dat

Before starting the update procedure is recommended to make backup copies of these files or the entire system.

**Requirements**:

* Android 2.3 (API 9) or later
* Superuser permissions (root)
* BusyBox

**Update procedure**:

1. Get superuser privileges (root).
2. Install BusyBox.
3. Check the connection to Internet.
4. Tap UPDATE button.
5. Restart your device.

**Referenses**:

* [Source code](https://github.com/meefik/tzupdater)
* [Releases](https://github.com/meefik/tzupdater/release)
* [Donations](http://meefik.github.io/donate/)
