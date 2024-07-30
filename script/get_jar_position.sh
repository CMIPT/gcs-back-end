#!/usr/bin/env bash

# NOTE: This file is for getting the jar file position after building the project
#       It should not be run directly, but should be called by other scripts

BUILD_DIR=$(mvn help:evaluate -Dexpression=project.build.directory -q -DforceStdout)
FINAL_NAME=$(mvn help:evaluate -Dexpression=project.build.finalName -q -DforceStdout)
PACKAGING=$(mvn help:evaluate -Dexpression=project.packaging -q -DforceStdout)

echo "${BUILD_DIR}/${FINAL_NAME}.${PACKAGING}"

