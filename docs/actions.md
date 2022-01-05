## Infrastructure
The necessary infrastructure for an execution can be defined in the benchmark manifests. The related resources are create *before* an execution is started, and removed *after* an execution is finished.

### Example

```yaml
  infrastructure:
    resources:
      - configMap:
          name: "example-configmap"
          files:
            - "uc1-kstreams-deployment.yaml"
```

## Action Commands
Theodolite allows to execute commands on running pods (similar to the `kubectl exec -it <pod-name> -- <command>` command). This commands can be run either before (via so called `beforeActions`) or after (via so called `afterActions`) an experiment is executed.

### Example

```yaml
# For the system under test
  sut:
    resources: ...
    beforeActions:
      - selector:
          pod:
            matchLabels:
              app: busybox1
        exec:
          command: ["touch", "test-file-sut"]
          timeoutSeconds: 90
    afterActions:
      - selector:
          pod:
            matchLabels:
              app: busybox1
        exec:
          command: [ "touch", "test-file-sut-after" ]
          timeoutSeconds: 90

# analog, for the load generator
  loadGenerator:
    resources: ... 
    beforeActions:
      - selector:
          pod:
            matchLabels:
              app: busybox1
        exec:
          command: ["touch", "test-file-loadGen"]
          timeoutSeconds: 90
    afterActions:
      - selector:
          pod:
            matchLabels:
              app: busybox1
        exec:
          command: [ "touch", "test-file-loadGen-after" ]
          timeoutSeconds: 90
```