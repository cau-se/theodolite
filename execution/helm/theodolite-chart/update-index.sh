#!/usr/bin/env sh

RELEASE_PATH="https://github.com/cau-se/theodolite/releases/download"
RELEASE_NAME=$1 # Supposed to be equal to tag, e.g., v0.3.0

helm repo index . --url $RELEASE_PATH/$RELEASE_NAME --merge ../../../docs/index.yaml && \
  mv index.yaml ../../../docs/index.yaml