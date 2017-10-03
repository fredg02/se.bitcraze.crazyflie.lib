#!/bin/bash

if ! [ -x "$(command -v xmlstarlet)" ]; then
  echo 'xmlstarlet is not installed. Trying to install it...' >&2
  sudo apt-get install xmlstarlet

# TODO: compile from source instead
#wget -N https://sourceforge.net/projects/xmlstar/files/xmlstarlet/1.6.1/xmlstarlet-1.6.1.tar.gz
#tar xzf xmlstarlet-*.tar.gz
#cd xmlstarlet-*
#./configure
#./make
fi

FULL_TARGET_REPO_PATH=`readlink -m ../se.bitcraze.crazyflie.lib-target/target/repository/`
echo "Full target repo path: $FULL_TARGET_REPO_PATH" 

TARGET_FILE=../se.bitcraze.crazyflie.lib-target/se.bitcraze.crazyflie.lib-target.target

# --inplace option requires at least version 1.0.2 of xmlstarlet
xmlstarlet ed --inplace -u "/target/locations/location/repository/@location" -v file:$FULL_TARGET_REPO_PATH $TARGET_FILE

# for xmlstarlet versions < 1.0.2
#xmlstarlet ed -u "/target/locations/location/repository/@location" -v file:$FULL_TARGET_REPO_PATH $TARGET_FILE > $TARGET_FILE.tmp
#mv $TARGET_FILE.tmp $TARGET_FILE