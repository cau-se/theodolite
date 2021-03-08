package theodolite.util

class Results {
    private val results: MutableMap<Pair<LoadDimension, Resource>, Boolean> = mutableMapOf()

    fun setResult(experiment: Pair<LoadDimension, Resource>, successful: Boolean) {
        this.results[experiment] = successful
    }

    fun getResult (experiment: Pair<LoadDimension, Resource>): Boolean? {
        return this.results[experiment]
    }

    fun getMinRequiredInstances(load: LoadDimension?, resourceTyp: String): Resource? {
        if (this.results.isEmpty()) return Resource(Int.MIN_VALUE, resourceTyp)

        var requiredInstances: Resource? = Resource(Int.MAX_VALUE, resourceTyp)
        for(experiment in results) {
            if(experiment.key.first == load && experiment.value){
                if(requiredInstances == null) {
                    requiredInstances = experiment.key.second
                }else if (experiment.key.second.get() < requiredInstances.get()) {
                    requiredInstances = experiment.key.second
                }
            }
        }
        return requiredInstances
    }

    fun getMaxBenchmarkedLoad(load: LoadDimension): LoadDimension? {
        var maxBenchmarkedLoad: LoadDimension? = null;
        for(experiment in results) {
            if (experiment.value) {
                if(experiment.key.first.get() <= load.get()) {
                    if (maxBenchmarkedLoad == null) {
                        maxBenchmarkedLoad = experiment.key.first
                    } else if (maxBenchmarkedLoad.get() < experiment.key.first.get()) {
                        maxBenchmarkedLoad = experiment.key.first
                    }
                }
            }
        }
        return maxBenchmarkedLoad
    }
}