#!/bin/bash

if [ -f './gradlew' ]; then
  GRADLE_EXE='./gradlew'
else
  GRADLE_EXE='../gradlew'
fi

echo ""
echo "GRADLE_EXE: ${GRADLE_EXE}"
echo "-----"
echo ""
