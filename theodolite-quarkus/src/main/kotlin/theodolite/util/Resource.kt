package theodolite.util

data class Resource(private val number: Int) {
    public fun get(): Int {
        return this.number;
    }
}