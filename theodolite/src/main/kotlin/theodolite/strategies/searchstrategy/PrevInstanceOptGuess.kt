package theodolite.strategies.searchstrategy


/**
 * [PrevInstanceOptGuess] is a [GuessStrategy] that implements the function [firstGuess],
 * where it returns the optimal result of the previous run.
 */

class PrevInstanceOptGuess() : GuessStrategy(){

    /**
     * If the metric is
     *
     * - "demand", [valuesToCheck] is a List of resources and [lastOptValue] a resource value.
     * - "capacity", [valuesToCheck] is a List of loads and [lastOptValue] a load value.
     *
     * @param valuesToCheck List of all possible resources/loads.
     * @param lastOptValue Previous minimal/maximal resource/load value for the given load/resource.
     *
     * @return the value of [lastOptValue] if given otherwise the first element of the [valuesToCheck] list or null
     */
    override fun firstGuess(valuesToCheck: List<Int>, lastOptValue: Int?): Int? {

        if (lastOptValue != null) return lastOptValue
        else if(valuesToCheck.isNotEmpty()) return valuesToCheck[0]
        else return null
    }
}