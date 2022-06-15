package rocks.theodolite.benchmarks.commons.kstreams;

import java.util.Map;
import java.util.function.Function;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Factory methods to create generic {@link Serde}s.
 */
public final class GenericSerde {

  private GenericSerde() {}

  /**
   * Create a {@link Serde} using a serialize and a deserialize function.
   *
   * @param serializer function to convert a record into a byte array
   * @param deserializer function to create a record from a byte array
   */
  public static <T> Serde<T> from(final Function<T, byte[]> serializer,
      final Function<byte[], T> deserializer) {
    return org.apache.kafka.common.serialization.Serdes.serdeFrom(new Serializer<T>() {

      @Override
      public void configure(final Map<String, ?> configs, final boolean isKey) {
        // Do nothing
      }

      @Override
      public byte[] serialize(final String topic, final T data) {
        return serializer.apply(data);
      }

      @Override
      public void close() {
        // Do nothing
      }
    }, new Deserializer<T>() {

      @Override
      public void configure(final Map<String, ?> configs, final boolean isKey) {
        // Do nothing
      }

      @Override
      public T deserialize(final String topic, final byte[] data) {
        if (data == null) {
          return null;
        }
        return deserializer.apply(data);
      }

      @Override
      public void close() {
        // Do nothing
      }

    });

  }

}
