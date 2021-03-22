package theodolite.util

data class LoadDimension(private val number: Int, private val type: List<PatcherDefinition>) {
    fun get(): Int {
        return this.number
    }

    fun getType(): List<PatcherDefinition> {
        return this.type
    }
}
