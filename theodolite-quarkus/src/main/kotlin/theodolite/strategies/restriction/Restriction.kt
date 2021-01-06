package theodolite.strategies.restriction

interface Restriction {
    fun restrict(loads: List<Int>, resources: List<Int>): List<Int>;
}