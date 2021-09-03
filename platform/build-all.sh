#!/bin/bash

source common-func.sh

set -x

java8

cd ./model-interface
./gradlew clean build publishToMavenLocal ; checkError
cd -

cd ./metrics
./gradlew clean build publishToMavenLocal ; checkError
cd -

cd ./testing/interfaces-impl
./gradlew clean build publishToMavenLocal ; checkError
cd -

java12

cd ./strategies
./gradlew clean build publishToMavenLocal ; checkError
cd -

cd ./coordinator/scripts
./buildAndCopySamples.sh ; checkError
cd -

cd ./coordinator/docker
./prepareImages.sh ; checkError
cd -

cd ./coordinator
./gradlew clean build ; checkError
cd -

echo "OK"
