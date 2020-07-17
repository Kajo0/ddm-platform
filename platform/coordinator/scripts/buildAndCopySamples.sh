#!/bin/bash

function checkError() {
  if [ $? -eq 1 ]; then
    echo "ERROR"
    exit 1
  fi
}

# FIXME setup depends on dev environment
export SDKMAN_DIR="/home/mmarkiew/.sdkman"
source "$SDKMAN_DIR/bin/sdkman-init.sh"

function java8() {
  set +x
  # FIXME setup depends on dev environment
  sdk use java 8.0.232-open
  set -x
}

function java12() {
  set +x
  # FIXME setup depends on dev environment
  sdk use java 12.0.2-open
  set -x
}

set -x

java8

cd ../../model-interface
./gradlew clean build publishToMavenLocal ; checkError
cd -

cd ../../algorithms/distance-functions
./gradlew clean build publishToMavenLocal ; checkError
cd -

cd ../../algorithms/clustering/aoptkm
./gradlew clean shadowJar ; checkError
cd -

cd ../../algorithms/clustering/dkmeans
./gradlew clean shadowJar ; checkError
cd -

cd ../../algorithms/clustering/lct
./gradlew clean shadowJar ; checkError
cd -

cd ../../algorithms/classification/dmeb
./gradlew clean shadowJar ; checkError
cd -

cd ../../algorithms/classification/dmeb-2
./gradlew clean shadowJar ; checkError
cd -

cd ../../algorithms/samples/random-classifier
./gradlew clean build ; checkError
cd -

cd ../../algorithms/samples/k-means-weka
./gradlew clean shadowJar ; checkError
cd -

cd ../../algorithms/samples/svm-weka
./gradlew clean shadowJar ; checkError
cd -

cd ../../algorithms/samples/equality-distance
./gradlew clean build ; checkError
cd -

java12

cd ../../algorithms/samples/dense-and-outliers-strategy
./gradlew clean shadowJar ; checkError
cd -

rm -rf ./samples/*.jar

cp ../../algorithms/clustering/aoptkm/build/libs/aoptkm-*-all.jar ./samples/aoptkm.jar ; checkError
cp ../../algorithms/clustering/dkmeans/build/libs/dkmeans-*-all.jar ./samples/dkmeans.jar ; checkError
cp ../../algorithms/clustering/lct/build/libs/lct-*-all.jar ./samples/lct.jar ; checkError
cp ../../algorithms/classification/dmeb/build/libs/dmeb-*-all.jar ./samples/dmeb.jar ; checkError
cp ../../algorithms/classification/dmeb-2/build/libs/dmeb-2-*-all.jar ./samples/dmeb-2.jar ; checkError
cp ../../algorithms/samples/random-classifier/build/libs/random-classifier-*.jar ./samples/random-classifier.jar ; checkError
cp ../../algorithms/samples/k-means-weka/build/libs/k-means-weka-*.jar ./samples/k-means-weka.jar ; checkError
cp ../../algorithms/samples/svm-weka/build/libs/svm-weka-*.jar ./samples/svm-weka.jar ; checkError

cp ../../algorithms/samples/equality-distance/build/libs/equality-distance-*.jar ./samples/equality-distance.jar ; checkError
cp ../../algorithms/samples/dense-and-outliers-strategy/build/libs/dense-and-outliers-strategy-*.jar ./samples/dense-and-outliers-strategy.jar ; checkError

echo "OK"
