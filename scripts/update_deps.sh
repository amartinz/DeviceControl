#!/bin/bash

if [ -f './config.sh' ]; then
  source ./config.sh
else
  source scripts/config.sh
fi

echo "===== Updating ====="
${GRADLE_EXE} assembleDebug --refresh-dependencies
echo "=====   DONE   ====="
