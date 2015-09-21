#!/bin/bash
mkdir tzcode
wget -O - https://www.iana.org/time-zones/repository/releases/tzcode2015f.tar.gz | tar xz -C tzcode
cd tzcode
make version.h
gcc zic.c -o zic -static
strip -s zic
