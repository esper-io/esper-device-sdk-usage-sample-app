#!/bin/bash

set -xeuo pipefail
APP_DIR=/application
LOG_FILE="$APP_DIR"/build_status.log
curr_script=$(basename -- "$0")
echo "Setting environment variable indicating that this is a Jenkins' build"
export BUILT_BY_JENKINS="yes"
echo "$curr_script started" | tee $LOG_FILE

echo "Copying cached files into the .gradle folder for faster builds"
mkdir -p  ~/.gradle/{caches,temp}
rm -rf ~/.gradle/caches
pushd ~/.gradle/temp
tar -xzf /gradle_dependency_cache/dependency_cache.tar.gz
popd
mv ~/.gradle/temp/home/nala/.gradle/caches ~/.gradle/caches
rm -rf ~/.gradle/temp

echo "Switching to the app directory" | tee -a $LOG_FILE
cd $APP_DIR
if [ $? -ne 0 ];then
   echo 'Failed to change to $APP_DIR directory' | tee -a $LOG_FILE
   exit
fi

echo "Setting the sdk path in local.properties" | tee -a $LOG_FILE
echo "sdk.dir=$ANDROID_SDK_DIR" > $ANDROID_PROJ_DIR/local.properties

echo "Making gradlew executable" | tee -a $LOG_FILE
chmod +x ./gradlew
if [ $? -ne 0 ];then
   echo 'Failed to mark gradlew as executable' | tee -a $LOG_FILE
   exit
fi

echo "Creating the wrapper based on what the app has requested" | tee -a $LOG_FILE
gradle wrapper
if [ $? -ne 0 ];then
   echo 'Failed to create wrapper' | tee -a $LOG_FILE
   exit
fi

export OBFUSCATION=true
export BUILD_OUTPUT_DIR=build-output

echo "Cleaning project" | tee -a $LOG_FILE
./gradlew clean
if [ $? -ne 0 ];then
   echo 'Failed to clean project' | tee -a $LOG_FILE
   exit
fi

# all flavours of release artifacts only.
echo "Building project" | tee -a $LOG_FILE
./gradlew assembleRelease --stacktrace
if [ $? -ne 0 ];then
   echo 'Failed to build project' | tee -a $LOG_FILE
   exit
fi


echo 'Build successful' | tee -a $LOG_FILE