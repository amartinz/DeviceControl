#!/bin/bash

if [ -f './config.sh' ]; then
  source ./config.sh
else
  source scripts/config.sh
fi

echo "===== Building ====="
${GRADLE_EXE} assembleRelease --refresh-dependencies
echo "=====   DONE   ====="
