package theodolite.util

data class Resource(val number: Int) {
    public fun get(): Int {
        return this.number;
    }
}