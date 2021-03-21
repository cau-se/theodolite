#!/usr/bin/env sh

RELEASE_NAME=$1 # Supposed to be equal to tag, e.g., v0.3.0

RELEASE_PATH="https://github.com/cau-se/theodolite/releases/download"
REPO_INDEX="../../docs/index.yaml"

helm repo index . --url $RELEASE_PATH/$RELEASE_NAME --merge $REPO_INDEX && \
  mv index.yaml $REPO_INDEX