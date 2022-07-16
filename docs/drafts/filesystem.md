## Creating a Benchmark with Filesystem Resources

#### Filesystem

Alternatively, resources can also be read from the filesystem, Theodolite has access to. This usually requires that the Benchmark resources are available in a volume, which is mounted into the Theodolite container.

```yaml
filesystem:
  path: example/path/to/files
  files:
  - example-deployment.yaml
  - example-service.yaml
```
