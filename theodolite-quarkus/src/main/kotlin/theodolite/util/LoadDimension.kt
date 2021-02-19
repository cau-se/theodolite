package theodolite.util

data class LoadDimension(private val number: Int) {
    public fun get(): Int {
        return this.number;
    }

    public fun getType(): String {
        return "NumSensors"
    }
}
