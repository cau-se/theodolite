Resource sets (`appResourceSets` or `loadGenResourceSets`) describe a set of Kubernetes resources.

They can be created from Kubernetes YAMl files placed in ConfigMaps or from files of the filesystem accessible by Theodolite.

### Creating Resources from ConfigMaps

```yaml
- configMap:
    name: "example-configmap"
    files:
        - "uc1-kstreams-deployment.yaml"
```

### Creating Resources from the Filesystem

```yaml
- fileSystem:
    path: ./../../../../../../config
    files:
      - "uc1-kstreams-deployment.yaml"
      - "aggregation-service.yaml"
      - "jmx-configmap.yaml"
      - "uc1-service-monitor.yaml"
```
