package theodolite.patcher

import io.fabric8.kubernetes.api.model.KubernetesResource

/**
 * A Patcher is able to modify values of a Kubernetes resource, see [Patcher].
 *
 * An AbstractPatcher is created with up to three parameters.
 *
 * @param k8sResource The Kubernetes resource to be patched.
 * @param container *(optional)* The name of the container to be patched
 * @param variableName *(optional)* The variable name to be patched
 *
 *
 * **For example** to patch the load dimension of a load generator, the patcher should be created as follow:
 *
 * k8sResource: `uc-1-workload-generator.yaml`
 * container: `workload`
 * variableName: `NUM_SENSORS`
 *
 */
abstract class AbstractPatcher(
    k8sResource: KubernetesResource
) : Patcher
