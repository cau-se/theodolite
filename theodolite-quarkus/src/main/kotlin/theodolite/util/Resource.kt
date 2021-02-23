package theodolite.util

data class Resource(private val number: Int, private val type: String) {
    public fun get(): Int {
        return this.number;
    }

    public fun getType(): String {
        return this.type
    }
}