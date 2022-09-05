---
title: Troubleshooting
has_children: false
parent: Running Benchmarks
nav_order: 10
---

# Troubleshooting Execution

## Verify your Installation

First you should make sure that all Theodolite pods are up and running by typing:

```sh
kubectl get pods
```

If pods are still creating or failing to start, you should simply wait a bit longer or investigate this further.

If Theodolite is [installed via Helm](installation.md), you can also use `helm test` to verify that everything is installed as expected. Just run the following command (replace `theodolite` by your release name) and wait for all tests to be completed:

```sh
helm test theodolite
```

## Getting Execution Events

Theodolite produces Kubernetes events, which you can view by running:

```sh
kubectl describe execution <your-execution-name>
```

## Looking the Operator Logs

If you cannot figure out why your benchmark execution fails, you might want to have look at the operator logs:

```sh
kubectl logs -l app=theodolite -c theodolite
```

You might also add the `-f` flag to follow the logs.

## Contact the Maintainers

Do not hesitate to [ask for help](project-info#getting-help) if you are still having problems. Attaching Theodolite's logs to your request is likely to help us solving your problem.
