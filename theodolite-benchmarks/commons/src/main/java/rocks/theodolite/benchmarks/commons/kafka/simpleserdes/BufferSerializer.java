package rocks.theodolite.benchmarks.commons.kafka.simpleserdes;

/**
 * Serializer to serialize an object to a {@link WriteBuffer}.
 *
 * @param <T> the type of the serialized object
 */
@FunctionalInterface
public interface BufferSerializer<T> {

  void serialize(WriteBuffer buffer, T data);

}
