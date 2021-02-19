package theodolite.patcher

interface Patcher {
    fun <T> patch(value: T)
}