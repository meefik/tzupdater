# tzupdater

Copyright (C) Anton Skshidlevsky, 2015

Licensed under the [GPL version 3](http://www.gnu.org/licenses/) or later.

This app downloads and updates a time zones to latest version on your device. Updated the following files:

* /data/misc/zoneinfo/tzdata 
* /system/usr/icu/*.dat

Before starting the update procedure is recommended to make backup copies of these files or the entire system.

Requirements:

* Android 4.3 (API 18) or later
* Superuser permissions (root)
* BusyBox

Update procedure:

1. Get superuser privileges (root).
2. Install BusyBox.
3. Check the connection to Internet.
4. Tap UPDATE button.
5. Restart your device.

Referenses:

* [Source code](https://github.com/meefik/tzupdater)
* [Releases](https://github.com/meefik/tzupdater/release)
* [Donations](http://meefik.github.io/donate/)
