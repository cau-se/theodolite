package rocks.theodolite.commons.kafka.simpleserdes;

/**
 * Deserializer to deserialize a {@link ReadBuffer} to an object.
 *
 * @param <T> the type of the deserialized object
 */
@FunctionalInterface
public interface BufferDeserializer<T> {

  T deserialize(ReadBuffer buffer);

}
