package rocks.theodolite.benchmarks.commons.kafka.simpleserdes;

import java.util.Map;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Kafka {@link Serializer} to be configured with a {@link BufferSerializer} for simpler usage.
 *
 * @param <T> the type of the serialized objects
 */
public class SimpleSerializer<T> implements Serializer<T> {

  private final BufferSerializer<T> serializer;

  public SimpleSerializer(final BufferSerializer<T> serializer) {
    this.serializer = serializer;
  }

  @Override
  public void configure(final Map<String, ?> configs, final boolean isKey) {
    // Do nothing
  }

  @Override
  public byte[] serialize(final String topic, final T data) {
    if (data == null) {
      return null;
    }
    final WriteBuffer buffer = new WriteBuffer();
    this.serializer.serialize(buffer, data);
    return buffer.getBytes();
  }

  @Override
  public void close() {
    // Do nothing
  }

}
