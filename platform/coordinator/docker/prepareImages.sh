#!/bin/bash

function checkError() {
  if [ $? -eq 1 ]; then
    echo "ERROR"
    exit 1
  fi
}

cd ../../node-agent
./gradlew clean build ; checkError
cd -

cd ../../app-runner-java
./gradlew clean shadowJar ; checkError
cd -

cp ../../node-agent/build/libs/node-agent-*.jar ./worker/apps/ ; checkError
cp ../../node-agent/build/libs/node-agent-*.jar ./master/apps/ ; checkError

# TODO remove - from classpath or distributed
cp ../../app-runner-java/build/libs/app-runner-java-*-all.jar ./master/apps/ ; checkError

docker image build -t ddm-platform-master ./master ; checkError
docker image build -t ddm-platform-worker ./worker ; checkError

echo "OK"
