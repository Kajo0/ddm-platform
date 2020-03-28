#!/bin/bash

function checkError() {
  if [ $? -eq 1 ]; then
    echo "ERROR"
    exit 1
  fi
}

cp ../../node-agent/build/libs/node-agent-0.0.1-SNAPSHOT.jar ./worker/apps/ ; checkError
cp ../../node-agent/build/libs/node-agent-0.0.1-SNAPSHOT.jar ./master/apps/ ; checkError

docker image build -t ddm-platform-master ./master ; checkError
docker image build -t ddm-platform-worker ./worker ; checkError

echo "OK"
