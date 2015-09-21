#!/bin/bash
wget -O - http://download.icu-project.org/files/icu4c/55.1/icu4c-55_1-src.tgz | tar xz
cd icu/source
./configure --enable-static --disable-shared
make LDFLAGS="-static"
strip -s bin/icupkg
