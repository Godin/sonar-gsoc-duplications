#!/bin/sh

# Analyse with new plugin
mvn sonar:sonar -Dsonar.dynamicAnalysis=false -Dsonar.skipDesign=true -Dsonar.branch=NEW -Dsonar.cpd.skip=true
