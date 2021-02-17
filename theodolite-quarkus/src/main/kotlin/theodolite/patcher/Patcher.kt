package theodolite.patcher

interface Patcher<T> {
    fun patch(value: T)
}