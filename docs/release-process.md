---
title: Release Process
has_children: false
nav_order: 2
---

# Release Process

This document describes how to perform a new Theodolite release.

We assume that we are creating the release `v0.3.1`. Please make sure to adjust
the following steps according to the release, you are actually performing.

1. Update `codemeta.json` to match the new version. In particular, make sure that `version` points to the version you are releasing and `dateModified` points to the date you are relasing this version. [CodeMeata generator](https://codemeta.github.io/codemeta-generator/) may help you in updating the file.

2. Create a new branch `v0.3` if it does not already exists. This branch will never
again be merged into master.

3. Checkout the `v0.3` branch.

4. Update all references to Theodolite Docker images to tag `v0.3.1`. These are the Kubernetes resource definitions in
`execution`, the references to *latest* in `run_uc.py`, the Docker Compose files in `docker-test` and the example `theodolite.yaml` job.

5. Commit these changes.

6. Tag this commit with `v0.3.1`. The corresponding Docker images will be uploaded.

7. Create *releases* for this tag in both, GitLab and GitHub.
