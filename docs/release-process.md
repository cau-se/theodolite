# Release Process

We assume that we are creating the release `v0.1.1`. Please make sure to adjust
the following steps according to the release, you are actually performing.

1. Create a new branch `v0.1` if not already exists. This branch will never
again be merged into master.

2. Checkout the `v0.1 branch.

3. Update all references to Theodolite Docker images to tag `v0-1-1`. These are
mainly the Kubernetes resource definitions in `execution` as well as the Docker
Compose files in `docker-test`.

4. Commit these changes.

5. Tag this commit with `v0.1.1`. The corresponding Docker images will be uploaded.
