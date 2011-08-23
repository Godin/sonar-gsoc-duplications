#!/bin/sh

mvn -V \
    -Denforcer.skip=true \
    --non-recursive \
    -Dsonar.workDir=/tmp/sonar \
    -Dsonar.configBackup=sonar-backup.xml \
    -Dsonar.runtimeVersion=2.9 \
    clean install org.codehaus.sonar:sonar-dev-maven-plugin:1.3.2:start-war
