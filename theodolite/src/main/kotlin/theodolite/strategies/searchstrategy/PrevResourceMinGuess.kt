package theodolite.strategies.searchstrategy


/**
 * This Guess strategy takes the minimal resource demand of the previous load, which is given as an argument for the
 * firstGuess function.
 */

class PrevResourceMinGuess() : GuessStrategy(){

    /**
     * @param resources List of all possible Resources.
     * @param lastLowestResource Previous resource demand needed for the given load.
     *
     * @return the value of lastLowestResource if given otherwise the first element of the resource list or null
     */
    override fun firstGuess(resources: List<Int>, lastLowestResource: Int?): Int? {

        if (lastLowestResource != null) return lastLowestResource
        else if(resources.isNotEmpty()) return resources[0]
        else return null
    }
}