#!/usr/bin/env sh

helm package . --dependency-update && rm -r charts # We don't want to include dependencies in our index
