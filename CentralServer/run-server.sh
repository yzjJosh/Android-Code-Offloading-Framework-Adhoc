#!/bin/bash

./gradlew clean
./gradlew build

java -Dlog4j.configurationFile=log4j2/configuration.xml -jar build/libs/CentralServer-1.0.jar &
