package rocks.theodolite.kubernetes

open class TheodoliteException (message: String, e: Exception? = null) : Exception(message,e)