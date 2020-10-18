#!/bin/bash

source common-func.sh

set -x

cd ./coordinator/scripts
./buildAndCopySamples.sh ; checkError
cd -

java8

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

cd ./coordinator/docker
./prepareImages.sh ; checkError
cd -

echo "OK"
