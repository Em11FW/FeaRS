#!/bin/sh
echo <ACCESS TOKEN> | docker login gitlab.reveal.si.usi.ch:60090 --username <USER NAME> --password-stdin

../gradlew --build-file=../build.gradle clean fatJar
../gradlew --build-file=../../webapp/build.gradle clean bootJar

docker build -t gitlab.reveal.si.usi.ch:60090/students/2020/valentina-ferrari/bachelorproject/fears-app ../
docker push     gitlab.reveal.si.usi.ch:60090/students/2020/valentina-ferrari/bachelorproject/fears-app

docker build -t gitlab.reveal.si.usi.ch:60090/students/2020/valentina-ferrari/bachelorproject/fears-webapp ../../webapp
docker push     gitlab.reveal.si.usi.ch:60090/students/2020/valentina-ferrari/bachelorproject/fears-webapp