package theodolite.util

import kotlin.properties.Delegates


class KafkaConfig() {
    lateinit var bootstrapSever: String
    lateinit var topics: List<Topic>

    class Topic() {
        lateinit var name: String
        var partition by Delegates.notNull<Int>()
        var replication by Delegates.notNull<Short>()

    }
}