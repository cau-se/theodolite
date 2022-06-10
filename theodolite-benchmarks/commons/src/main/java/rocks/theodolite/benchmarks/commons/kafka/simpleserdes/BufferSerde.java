package rocks.theodolite.benchmarks.commons.kafka.simpleserdes;

/**
 * Combine {@link BufferSerializer} and {@link BufferDeserializer} into one type. This allows
 * implementing a serde in one class.
 *
 * @param <T> Type of the serializer and deserializer.
 */
public interface BufferSerde<T> extends BufferSerializer<T>, BufferDeserializer<T> {

}
