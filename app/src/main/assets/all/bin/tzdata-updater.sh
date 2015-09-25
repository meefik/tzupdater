#!/system/bin/sh
# Time zones updater for Android
# (c) 2015 Anton Skshidlevsky <meefik@gmail.com>, GPLv3

TZ_VERSION="$1"
[ -n "${ENV_DIR}" ] || ENV_DIR="."
OUTPUT_DIR="${ENV_DIR}/tmp"
TZ_DATA="/data/misc/zoneinfo/tzdata"
TZ_EXTRACTED="${OUTPUT_DIR}/extracted"
TZ_COMPILED="${OUTPUT_DIR}/compiled"

tz_version()
{
REPO_URL="http://www.iana.org/time-zones"
if [ -z "${TZ_VERSION}" ]; then
   printf "Getting latest version ... "
   TZ_VERSION=$(wget -q -O - ${REPO_URL} | grep -o '[0-9]\{4\}[a-z]\{1\}' | sort | tail -1)
   [ -n "${TZ_VERSION}" ] && printf "done\n" || { printf "fail\n"; return 1; }
fi
printf "Found tzdata version: ${TZ_VERSION}\n"
return 0
}

mk_output()
{
[ -e "${OUTPUT_DIR}" ] || mkdir ${OUTPUT_DIR}
[ -e "${TZ_EXTRACTED}" ] || mkdir ${TZ_EXTRACTED}
[ -e "${TZ_COMPILED}" ] || mkdir ${TZ_COMPILED}
}

download()
{
printf "Downloading tzdata${TZ_VERSION}.tar.gz ... "
wget -q -O - "http://www.iana.org/time-zones/repository/releases/tzdata${TZ_VERSION}.tar.gz" | tar xz -C ${TZ_EXTRACTED}
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
cat ${TZ_FILES} | grep '^Link' | awk '{print $3}') | LC_ALL=C sort) > ${OUTPUT_DIR}/setup
[ -e "${OUTPUT_DIR}/setup" ] && printf "done\n" || { printf "fail\n"; return 1; }
return 0
}

compile()
{
printf "Compiling timezones ... "
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
if [ -e "${TZ_DATA}" ]; then
   cp ${TZ_DATA} ${TZ_DATA}.bak
fi
[ $? -eq 0 ] && printf "done\n" || { printf "fail\n"; return 1; }
return 0
}

restore()
{
printf "Restoring tzdata ... "
if [ -e "${TZ_DATA}.bak" ]; then
   cp ${TZ_DATA}.bak ${TZ_DATA}
fi
[ $? -eq 0 ] && printf "done\n" || { printf "fail\n"; return 1; }
return 0
}

update()
{
printf "Updating tzdata ... "
dalvikvm -cp ${ENV_DIR}/bin/ZoneCompactor.dex ZoneCompactor ${OUTPUT_DIR}/setup ${TZ_COMPILED} ${TZ_EXTRACTED}/zone.tab ${TZ_DATA} tzdata${TZ_VERSION}
[ $? -eq 0 ] && printf "done\n" || { printf "fail\n"; return 1; }
printf "Set permissions ... "
chmod 644 ${TZ_DATA}
[ $? -eq 0 ] && printf "done\n" || { printf "fail\n"; return 1; }
return 0
}

cleanup()
{
printf "Cleaning ... "
rm /data/misc/zoneinfo/*.bak
rm -rf "${OUTPUT_DIR}"
[ $? -eq 0 ] && printf "done\n" || { printf "fail\n"; return 1; }
return 0
}

error()
{
printf "An error has occurred. Exiting.\n"
exit 1
}

tz_version || error
mk_output
download || { cleanup; error; }
scan_files || { cleanup; error; }
setup_file || { cleanup; error; }
compile || { cleanup; error; }
backup || { cleanup; error; }
update || { restore; cleanup; error; }
cleanup

exit 0
