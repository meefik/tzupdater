#!/bin/bash
# ZoneCompactor
# https://android.googlesource.com/platform/bionic/+/lollipop-release/libc/tools/zoneinfo/ZoneCompactor.java
# https://android.googlesource.com/platform/libcore/+/lollipop-release/luni/src/main/java/libcore/util/ZoneInfo.java
# https://android.googlesource.com/platform/libcore/+/lollipop-release/luni/src/main/java/libcore/io/BufferIterator.java
if [ "$1" == "clean" ]; then
find . -name "*.class" -exec rm {} \;
else
DX="$HOME/android-sdk-linux/build-tools/*/dx"
javac -source 1.5 -target 1.5 -d . ZoneCompactor.java libcore/util/ZoneInfo.java libcore/io/BufferIterator.java
${DX} --dex --output=ZoneCompactor.dex $(find . -name '*.class' -type f)
fi
