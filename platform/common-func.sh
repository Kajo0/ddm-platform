#!/bin/bash

function checkError() {
  code=$?
  set +x
  if [ $code -ne 0 ]; then
    echo "ERROR"
    exit 1
  fi
  set -x
}

# FIXME setup depends on dev environment
export SDKMAN_DIR="/home/${USER}/.sdkman"
source "$SDKMAN_DIR/bin/sdkman-init.sh"

function java8() {
  set +x
  # FIXME setup depends on dev environment
  sdk use java 8.0.265-open
  set -x
}

function java12() {
  set +x
  # FIXME setup depends on dev environment
  sdk use java 12.0.2-open
  set -x
}
