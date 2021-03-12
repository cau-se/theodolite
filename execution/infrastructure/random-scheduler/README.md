# Theodolite Random Scheduler
This directory contains the Theodolite Random Scheduler that schedules pods on random nodes.

## Build and Push
Run the following commands

- `docker build -t theodolite-random-scheduler .`
- `docker tag theodolite-random-scheduler <user>/theodolite-random-scheduler`
- `docker push <user>/theodolite-random-scheduler`

## Deployment
Deploy the `deployment.yaml` file into Kubernetes. Note, that the `TARGET_NAMESPACE` environment variable specifies the operating namespace of the random scheduler.
