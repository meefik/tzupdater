#!/bin/bash
mkdir tzcode
wget -O - https://data.iana.org/time-zones/tzcode-latest.tar.gz | tar xz -C tzcode
cd tzcode
make version.h
gcc zic.c -o zic -static
strip -s zic
