# Theodolite Random Scheduler

This directory contains the Theodolite Random Scheduler that schedules pods on random nodes.

## Build and Publish

Run the following commands

- `docker build -t theodolite-random-scheduler .`
- `docker tag theodolite-random-scheduler <repo>/theodolite-random-scheduler`
- `docker push <repo>/theodolite-random-scheduler`

## Deployment

Deploy the `deployment.yaml` file into Kubernetes. Note, that the `TARGET_NAMESPACE` environment variable specifies the operating namespace of the random scheduler.
