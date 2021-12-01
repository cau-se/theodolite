package theodolite.util

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.quarkus.runtime.annotations.RegisterForReflection
import theodolite.util.KafkaConfig.TopicWrapper
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

/**
 * Configuration of Kafka connection.
 *
 * @see TopicWrapper
 */
@RegisterForReflection
@JsonDeserialize
class KafkaConfig {
    /**
     * The bootstrap server connection string
     */
    lateinit var bootstrapServer: String

    /**
     * The list of topics
     */
    lateinit var topics: List<TopicWrapper>

    /**
     * Wrapper for a topic definition.
     */
    @RegisterForReflection
    @JsonDeserialize
    class TopicWrapper {
        /**
         * The topic name
         */
        lateinit var name: String

        /**
         * The number of partitions
         */
        var numPartitions by Delegates.notNull<Int>()

        /**
         * The replication factor of this topic
         */
        var replicationFactor by Delegates.notNull<Short>()

        /**
         * If remove only, this topic would only used to delete all topics, which has the name of the topic as a prefix.
         */
        var removeOnly by DelegatesFalse()
    }
}

/**
 * Delegates to initialize a lateinit boolean to false
 */
@RegisterForReflection
class DelegatesFalse {
    private var state = false
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return state
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        state = value
    }

}
