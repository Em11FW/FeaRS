#!/bin/bash

bp.crawler/gradlew --build-file=bp.crawler/build.gradle clean fatJar
bp.analyser/gradlew --build-file=bp.analyser/build.gradle clean fatJar

cp -v bp.crawler/build/libs/*.jar feaRS/src/main/resources/
cp -v bp.analyser/build/libs/*.jar feaRS/src/main/resources/

feaRS/gradlew --build-file=feaRS/build.gradle clean fatJar

# cd webapp && ./gradlew clean bootJar && ../
# java -jar webapp/build/libs/*.jar
