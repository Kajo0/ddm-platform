#!/bin/bash

source ../../common-func.sh

set -x

cd ../../metrics
./gradlew clean build publishToMavenLocal ; checkError
cd -

cd ../../model-interface
./gradlew clean build publishToMavenLocal ; checkError
cd -

cd ../../distance-functions
./gradlew clean build publishToMavenLocal ; checkError
cd -

cd ../../node-agent
./gradlew clean build ; checkError
cd -

cd ../../app-runner-java
./gradlew clean shadowJar ; checkError
cd -

mkdir -p ./master/apps
mkdir -p ./worker/apps

cp ../../node-agent/build/libs/node-agent-*.jar ./master/apps/ ; checkError
cp ../../node-agent/build/libs/node-agent-*.jar ./worker/apps/ ; checkError

# TODO remove - from classpath or distributed
cp ../../app-runner-java/build/libs/app-runner-java-*-all.jar ./master/apps/ ; checkError

docker image build -t ddm-platform-master ./master ; checkError
docker image build -t ddm-platform-worker ./worker ; checkError

echo "OK"
