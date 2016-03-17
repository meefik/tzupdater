#!/bin/bash
# ZoneCompactor
# https://android.googlesource.com/platform/bionic/+/jb-release/libc/tools/zoneinfo/ZoneCompactor.java
# https://android.googlesource.com/platform/bionic/+/jb-release/libc/tools/zoneinfo/ZoneInfo.java
if [ "$1" == "clean" ]; then
find . -name "*.class" -exec rm {} \;
else
[ -z "$ANDROID_SDK_ROOT" ] && ANDROID_SDK_ROOT="$HOME/Android/Sdk"
DX="$ANDROID_SDK_ROOT/build-tools/*/dx"
javac -source 1.3 -target 1.3 -d . ZoneCompactor.java ZoneInfo.java
${DX} --dex --output=ZoneCompactorLegacy.dex $(find . -name '*.class' -type f)
fi
