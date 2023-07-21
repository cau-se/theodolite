---
title: Release Process
has_children: false
parent: Contributing
nav_order: 1
---

# Release Process

This document describes how to perform a new Theodolite release.

We assume that we are creating the release `v0.3.1`. Please make sure to adjust
the following steps according to the release, you are actually performing.

1. Create a new branch `v0.3` if it does not already exist. This branch will never
again be merged into main.

2. Checkout the `v0.3` branch.

3. Update all references to artifacts which are versioned. This includes:

    1. Update all references to Theodolite Docker images to tag `v0.3.1`. These are:
        1. the default `helm/values.yaml` file,
        2. the example `execution/theodolite.yaml` job,
        3. the Kubernetes benchmark resources in `theodolite-benchmarks/definitions/**/resources` and
        4. the Docker Compose files in `theodolite-benchmarks/docker-test`.

    2. Update both, the `version` and the `appVersion` fields, in the Helm `Charts.yaml` file to `0.3.1`.

    3. Update the `version` field of the `theodolite/build.gradle` file to `0.3.1`. Make sure to also adjust all references to the build artifact in the `theodolite/README.md`.

    4. Update `codemeta.json` to match the new version. In particular, make sure that `version` points to the version you are releasing and `dateModified` points to the date you are relasing this version. [CodeMeta generator](https://codemeta.github.io/codemeta-generator/) may help you in updating the file.

    5. Update `CITATION.cff` to match the new version. At least update the `version` field.

4. Create a Helm package by running `./build-package.sh` from the chart directory.

5. Update the Helm repository index of located at `/docs` by running `./update-index.sh v0.3.1`.

6. Commit these changes to the `v0.3` branch.

7. Tag this commit `v0.3.1` (can be done via GitLab). The corresponding Docker images will be uploaded from the CI/CD pipeline.

8. Create *releases* on GitLab and GitHub. Upload the generated Helm package to these releases via the UIs of GitLab and GitHub.

9. Switch to the `main` branch.

10. Re-run `./update-index.sh v0.3.1` to include the latest release in the *upstream* Helm repository. You can now delete the packaged Helm chart.

11. If this release increments Theodolite's *latest* version number, 

    1. Update the Helm `Charts.yaml` file to `0.4.0-SNAPSHOT` (see Step 3).

    2. Update the Theodolite `build.gradle` and `README.md` files `0.4.0-SNAPSHOT` (see Step 3).

    3. Update the `codemeta.json` file according to Step 3.

    4. Update the `CITATION.cff` file according to Step 3.

12. Commit these changes to the `main` branch.
