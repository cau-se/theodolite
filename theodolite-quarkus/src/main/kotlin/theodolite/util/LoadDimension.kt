package theodolite.util

data class LoadDimension(private val number: Int, private val type: String) {
    fun get(): Int {
        return this.number;
    }

    fun getType(): String {
        return this.type
    }
}
