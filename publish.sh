#!/bin/bash

if [ -z "$BINTRAY_USER" ]; then
  printf "Missing BINTRAY_USER environment variable\n"
  exit 1
fi

if [ -z "$BINTRAY_API_KEY" ]; then
  printf "Missing BINTRAY_API_KEY environment variable, you can get API KEY at https://bintray.com/profile/edit\n"
  exit 1
fi

sh gradlew --no-daemon bintrayUpload

printf "Done!\n"

