package theodolite.util

class Resource(val number: Int) {
    public fun get(): Int {
        return this.number;
    }

    public override fun equals(other: Any?): Boolean {
        if (other is Resource) {
            return this.get() == other.get()
        }
        return false
    }

    override fun hashCode(): Int {
        return this.get().hashCode()
    }
}