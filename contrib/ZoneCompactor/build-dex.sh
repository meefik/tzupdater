#!/bin/bash
if [ "$1" == "clean" ]; then
find . -name "*.class" -exec rm {} \;
else
DX="$HOME/android-sdk-linux/build-tools/*/dx"
javac -source 1.6 -target 1.6 -d . ZoneCompactor.java libcore/util/ZoneInfo.java libcore/io/BufferIterator.java
${DX} --dex --output=ZoneCompactor.dex $(find . -name '*.class' -type f)
fi
