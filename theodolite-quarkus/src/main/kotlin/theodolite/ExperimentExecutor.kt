package theodolite

class ExperimentExecutor(
    val config: ExperimentConfig
) {
    fun run() {
        this.config.restrictionStrategy.restrictResource();
    }
}