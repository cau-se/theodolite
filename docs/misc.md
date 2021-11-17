
Not sure yet where to put the following docs.

### Use results-access Sidecar

```sh
kubectl cp $(kubectl get pod -l app=theodolite -o jsonpath="{.items[0].metadata.name}"):/results . -c results-access
```