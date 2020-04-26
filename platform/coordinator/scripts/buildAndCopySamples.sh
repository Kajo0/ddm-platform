#!/bin/bash

function checkError() {
  if [ $? -eq 1 ]; then
    echo "ERROR"
    exit 1
  fi
}

cd ../../algorithms/clustering/aoptkm
./gradlew clean shadowJar ; checkError
cd -

cd ../../algorithms/samples/random-classifier
./gradlew clean build ; checkError
cd -

cd ../../algorithms/samples/k-means-weka
./gradlew clean shadowJar ; checkError
cd -

cd ../../algorithms/samples/equality-distance
./gradlew clean build ; checkError
cd -

cp ../../algorithms/clustering/aoptkm/build/libs/aoptkm-*-all.jar ./samples/aoptkm.jar ; checkError
cp ../../algorithms/samples/random-classifier/build/libs/random-classifier-*.jar ./samples/random-classifier.jar ; checkError
cp ../../algorithms/samples/k-means-weka/build/libs/k-means-weka-*.jar ./samples/k-means-weka.jar ; checkError

cp ../../algorithms/samples/equality-distance/build/libs/equality-distance-*.jar ./samples/equality.jar ; checkError

echo "OK"
