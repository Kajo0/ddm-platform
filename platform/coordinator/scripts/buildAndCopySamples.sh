#!/bin/bash

source ../../common-func.sh

set -x

java8

cd ../../distance-functions
./gradlew clean build publishToMavenLocal ; checkError
cd -

cd ../../algorithms/clustering/aoptkm
./gradlew clean shadowJar publishToMavenLocal ; checkError
cd -

cd ../../algorithms/clustering/dkmeans
./gradlew clean shadowJar publishToMavenLocal ; checkError
cd -

cd ../../algorithms/clustering/lct
./gradlew clean shadowJar publishToMavenLocal ; checkError
cd -

cd ../../algorithms/classification/dmeb
./gradlew clean shadowJar publishToMavenLocal ; checkError
cd -

cd ../../algorithms/classification/dmeb-2
./gradlew clean shadowJar publishToMavenLocal ; checkError
cd -

cd ../../algorithms/classification/svm-2lvl
./gradlew clean shadowJar publishToMavenLocal ; checkError
cd -

cd ../../algorithms/classification/naive-bayes
./gradlew clean shadowJar publishToMavenLocal ; checkError
cd -

cd ../../algorithms/samples/random-classifier
./gradlew clean build publishToMavenLocal ; checkError
cd -

cd ../../algorithms/samples/k-means-weka
./gradlew clean shadowJar publishToMavenLocal ; checkError
cd -

cd ../../algorithms/samples/svm-weka
./gradlew clean shadowJar publishToMavenLocal ; checkError
cd -

cd ../../algorithms/samples/equality-distance
./gradlew clean build publishToMavenLocal ; checkError
cd -

java12

cd ../../algorithms/samples/dense-and-outliers-strategy
./gradlew clean shadowJar publishToMavenLocal ; checkError
cd -

rm -rf ./samples/*.jar

cp ../../algorithms/clustering/aoptkm/build/libs/aoptkm-*-all.jar ./samples/aoptkm.jar ; checkError
cp ../../algorithms/clustering/dkmeans/build/libs/dkmeans-*-all.jar ./samples/dkmeans.jar ; checkError
cp ../../algorithms/clustering/lct/build/libs/lct-*-all.jar ./samples/lct.jar ; checkError
cp ../../algorithms/classification/dmeb/build/libs/dmeb-*-all.jar ./samples/dmeb.jar ; checkError
cp ../../algorithms/classification/dmeb-2/build/libs/dmeb-2-*-all.jar ./samples/dmeb-2.jar ; checkError
cp ../../algorithms/classification/svm-2lvl/build/libs/svm-2lvl-*-all.jar ./samples/svm-2lvl.jar ; checkError
cp ../../algorithms/classification/naive-bayes/build/libs/naive-bayes-*-all.jar ./samples/naive-bayes.jar ; checkError
cp ../../algorithms/samples/random-classifier/build/libs/random-classifier-*.jar ./samples/random-classifier.jar ; checkError
cp ../../algorithms/samples/k-means-weka/build/libs/k-means-weka-*-all.jar ./samples/k-means-weka.jar ; checkError
cp ../../algorithms/samples/svm-weka/build/libs/svm-weka-*-all.jar ./samples/svm-weka.jar ; checkError

cp ../../algorithms/samples/equality-distance/build/libs/equality-distance-*.jar ./samples/equality-distance.jar ; checkError
cp ../../algorithms/samples/dense-and-outliers-strategy/build/libs/dense-and-outliers-strategy-*-all.jar ./samples/dense-and-outliers-strategy.jar ; checkError

echo "OK"
