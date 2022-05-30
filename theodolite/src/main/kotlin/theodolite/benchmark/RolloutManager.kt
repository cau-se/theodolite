package theodolite.benchmark

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.apps.DaemonSet
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.ReplicaSet
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.api.model.batch.v1.Job
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import theodolite.k8s.K8sManager

private var SLEEP_TIME_MS = 500L


class RolloutManager(private val blockUntilResourcesReady: Boolean, private val client: NamespacedKubernetesClient) {

    fun rollout(resources: List<HasMetadata>) {
        resources
            .forEach { K8sManager(client).deploy(it) }

        if (blockUntilResourcesReady) {
            resources
                .forEach {
                    when (it) {
                        is Deployment -> waitFor { client.apps().deployments().withName(it.metadata.name).isReady }
                        is StatefulSet -> waitFor { client.apps().statefulSets().withName(it.metadata.name).isReady }
                        is DaemonSet -> waitFor { client.apps().daemonSets().withName(it.metadata.name).isReady }
                        is ReplicaSet -> waitFor { client.apps().replicaSets().withName(it.metadata.name).isReady }
                        is Job -> waitFor { client.batch().v1().cronjobs().withName(it.metadata.name).isReady }
                    }
                }
        }
    }

    private fun waitFor(isResourceReady: () -> Boolean) {
        while (!isResourceReady()) {
            Thread.sleep(SLEEP_TIME_MS)
        }
    }

}