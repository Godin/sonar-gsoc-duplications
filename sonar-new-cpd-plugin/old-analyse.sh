#!/bin/sh

# Analyse with old plugin
mvn sonar:sonar -Dsonar.dynamicAnalysis=false -Dsonar.skipDesign=true -Dsonar.branch=OLD -Dsonar.newcpd.skip=true
