#!/system/bin/sh
# ICU data updater for Android
# (c) 2015 Anton Skshidlevsky <meefik@gmail.com>, GPLv3

TZ_VERSION="$1"
ICU_DIR="/system/usr/icu"
DAT_FILES=$(cd ${ICU_DIR}; ls *.dat)
[ -n "${DAT_FILES}" ] || { printf "ICU data not found.\n"; exit 1; }
[ -n "${ENV_DIR}" ] || ENV_DIR="."
OUTPUT_DIR="${ENV_DIR}/tmp"

icu_version()
{
REPO_URL="http://source.icu-project.org/repos/icu/data/trunk/tzdata/icunew"
if [ -z "${TZ_VERSION}" ]; then
   printf "Getting latest version ... "
   TZ_VERSION=$(wget -q -O - ${REPO_URL} | grep -o '[0-9]\{4\}[a-z]\{1\}' | sort | tail -1)
   [ -n "${TZ_VERSION}" ] && printf "done\n" || { printf "fail\n"; return 1; }
fi
printf "Found ICU version: ${TZ_VERSION}\n"
ICU_URL="${REPO_URL}/${TZ_VERSION}/44/le"
RES_FILES="zoneinfo64.res windowsZones.res timezoneTypes.res metaZones.res"
return 0
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

backup()
{
for dat in ${DAT_FILES}
do
   printf "Backuping ${dat} ... "
   if [ -e "${ICU_DIR}/${dat}" ]; then
      cp ${ICU_DIR}/${dat} ${OUTPUT_DIR}/${dat}
   fi
   [ $? -eq 0 ] && printf "done\n" || { printf "fail\n"; return 1; }
done
return 0
}

restore()
{
for dat in ${DAT_FILES}
do
   printf "Restoring ${dat} ... "
   if [ -e "${OUTPUT_DIR}/${dat}" ]; then
      cp ${OUTPUT_DIR}/${dat} ${ICU_DIR}/${dat}
   fi
   [ $? -eq 0 ] && printf "done\n" || { printf "fail\n"; return 1; }
done
return 0
}

download()
{
[ -d "${OUTPUT_DIR}" ] || mkdir -p ${OUTPUT_DIR}
for res in ${RES_FILES}
do
   printf "Downloading ${res} ... "
   wget -q ${ICU_URL}/${res} -O ${OUTPUT_DIR}/${res}
   [ $? -eq 0 ] && printf "done\n" || { printf "fail\n"; return 1; }
done
return 0
}

update()
{
for dat in ${DAT_FILES}
do
   printf "Updating ${dat} ... "
   for res in ${RES_FILES}
   do
      icupkg -s ${OUTPUT_DIR} -a ${res} ${ICU_DIR}/${dat}
      [ $? -eq 0 ] || { printf "fail\n"; return 1; }
   done
   printf "done\n"
done
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

icu_version || error
download || { cleanup; error; }
mount_rw || { cleanup; error; }
backup || { cleanup; mount_ro; error; }
update || { restore; cleanup; mount_ro; error; }
cleanup
mount_ro

sync

exit 0
