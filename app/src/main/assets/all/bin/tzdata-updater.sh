#!/system/bin/sh
# Time zones updater for Android
# (c) 2015 Anton Skshidlevsky <meefik@gmail.com>, GPLv3

TZ_VERSION="$1"
[ -n "${ENV_DIR}" ] || ENV_DIR="."
OUTPUT_DIR="${ENV_DIR}/tmp"
TZ_EXTRACTED="${OUTPUT_DIR}/extracted"
TZ_COMPILED="${OUTPUT_DIR}/compiled"
TZ_SETUP="${OUTPUT_DIR}/setup"

android_version()
{
if [ -e "/system/usr/share/zoneinfo/tzdata" ]; then
   echo "new"
elif [ -e "/system/usr/share/zoneinfo/zoneinfo.dat" ]; then
   echo "legacy"
fi
}

mount_rw()
{
printf "Remount /system to rw ... "
mount -o rw,remount /system
[ $? -eq 0 ] && printf "done\n" || { printf "fail\n"; return 1; }
return 0
}

mount_ro()
{
printf "Remount /system to ro ... "
mount -o ro,remount /system
[ $? -eq 0 ] && printf "done\n" || { printf "skip\n"; return 1; }
return 0
}

tz_version()
{
if [ -z "${TZ_VERSION}" ]; then
   printf "Getting latest version ... "
   TZ_VERSION=$(wget -q -O - "http://data.iana.org/time-zones/" | grep -o '[0-9]\{4\}[a-z]\{1\}' | sort -u | tail -n1)
   [ -n "${TZ_VERSION}" ] && printf "done\n" || { printf "fail\n"; return 1; }
fi
printf "Found tzdata version: ${TZ_VERSION}\n"
return 0
}

download()
{
printf "Downloading tzdata${TZ_VERSION}.tar.gz ... "
[ -e "${TZ_EXTRACTED}" ] || mkdir -p ${TZ_EXTRACTED}
wget -q -O - "http://data.iana.org/time-zones/releases/tzdata${TZ_VERSION}.tar.gz" | tar xz -C ${TZ_EXTRACTED}
[ $? -eq 0 ] && printf "done\n" || { printf "fail\n"; return 1; }
return 0
}

scan_files()
{
printf "Scaning timezone files ... "
TZ_FILES=$(find ${TZ_EXTRACTED} -type f ! -name 'backzone' | LC_ALL=C sort |
while read f
do
   if [ $(grep -c '^Link' $f) -gt 0 -o $(grep -c '^Zone' $f) -gt 0 ]; then
      echo $f
   fi
done)
[ -n "${TZ_FILES}" ] && printf "done\n" || { printf "fail\n"; return 1; }
return 0
}

setup_file()
{
printf "Generating setup file ... "
(cat ${TZ_FILES} | grep '^Link' | awk '{print $1" "$2" "$3}'
(cat ${TZ_FILES} | grep '^Zone' | awk '{print $2}'
cat ${TZ_FILES} | grep '^Link' | awk '{print $3}') | LC_ALL=C sort) > ${TZ_SETUP}
[ -e "${TZ_SETUP}" ] && printf "done\n" || { printf "fail\n"; return 1; }
return 0
}

compile()
{
printf "Compiling timezones ... "
[ -e "${TZ_COMPILED}" ] || mkdir -p ${TZ_COMPILED}
for tzfile in ${TZ_FILES}
do
   [ "${tzfile##*/}" == "backward" ] && continue
   zic -d ${TZ_COMPILED} ${tzfile}
   [ $? -ne 0 ] && { printf "fail\n"; return 1; }
done
printf "done\n"
return 0
}

backup()
{
printf "Backuping tzdata ... "
for tzdata in ${TZDATA_FILES}
do
if [ -e "${ZONEINFO_DIR}/${tzdata}" ]; then
   cp ${ZONEINFO_DIR}/${tzdata} ${OUTPUT_DIR}/${tzdata}
   [ $? -eq 0 ] || { printf "fail\n"; return 1; }
fi
done
printf "done\n"
return 0
}

restore()
{
printf "Restoring tzdata ... "
for tzdata in ${TZDATA_FILES}
do
if [ -e "${OUTPUT_DIR}/${tzdata}" ]; then
   cp ${OUTPUT_DIR}/${tzdata} ${ZONEINFO_DIR}/${tzdata}
   [ $? -eq 0 ] || { printf "fail\n"; return 1; }
fi
done
printf "done\n"
return 0
}

update()
{
printf "Updating tzdata ... "
[ -e "${ZONEINFO_DIR}" ] || mkdir ${ZONEINFO_DIR}
dalvikvm -cp ${ENV_DIR}/bin/ZoneCompactor.dex ZoneCompactor ${TZ_SETUP} ${TZ_COMPILED} ${TZ_EXTRACTED}/zone.tab ${ZONEINFO_DIR} ${TZ_VERSION}
[ $? -eq 0 ] && printf "done\n" || { printf "fail\n"; return 1; }
return 0
}

update_legacy()
{
printf "Updating tzdata ... "
[ -e "${ZONEINFO_DIR}" ] || mkdir ${ZONEINFO_DIR}
dalvikvm -cp ${ENV_DIR}/bin/ZoneCompactorLegacy.dex ZoneCompactor ${TZ_SETUP} ${TZ_COMPILED} ${ZONEINFO_DIR} ${TZ_VERSION}
[ $? -eq 0 ] && printf "done\n" || { printf "fail\n"; return 1; }
return 0
}

set_permissions()
{
printf "Set permissions ... "
chmod 755 ${ZONEINFO_DIR}
chmod 644 ${ZONEINFO_DIR}/*
[ $? -eq 0 ] && printf "done\n" || { printf "fail\n"; return 1; }
return 0
}

cleanup()
{
printf "Cleaning ... "
rm -r "${OUTPUT_DIR}"
[ $? -eq 0 ] && printf "done\n" || { printf "fail\n"; return 1; }
return 0
}

error()
{
printf "An error has occurred. Exiting.\n"
exit 1
}

case $(android_version) in
new)
   ZONEINFO_DIR="/data/misc/zoneinfo"
   TZDATA_FILES="tzdata"
   tz_version || error
   download || { cleanup; error; }
   scan_files || { cleanup; error; }
   setup_file || { cleanup; error; }
   compile || { cleanup; error; }
   backup || { cleanup; error; }
   update || { restore; cleanup; error; }
   set_permissions || { restore; cleanup; error; }
   cleanup
;;
legacy)
   ZONEINFO_DIR="/system/usr/share/zoneinfo"
   TZDATA_FILES="zoneinfo.dat zoneinfo.idx zoneinfo.version"
   tz_version || error
   download || { cleanup; error; }
   scan_files || { cleanup; error; }
   setup_file || { cleanup; error; }
   compile || { cleanup; error; }
   mount_rw || { cleanup; error; }
   backup || { cleanup; mount_ro; error; }
   update_legacy || { restore; cleanup; mount_ro; error; }
   set_permissions || { restore; cleanup; mount_ro; error; }
   cleanup
   mount_ro
;;
*)
   printf "Time zones database not found.\n"
   error
;;
esac

sync

exit 0
