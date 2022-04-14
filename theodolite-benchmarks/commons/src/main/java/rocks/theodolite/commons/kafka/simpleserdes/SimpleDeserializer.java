package rocks.theodolite.commons.kafka.simpleserdes;

import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Kafka {@link Deserializer} to be configured with a {@link BufferDeserializer} for simpler usage.
 *
 * @param <T> the type of the deserialized object
 */
public class SimpleDeserializer<T> implements Deserializer<T> {

  private final BufferDeserializer<T> deserializer;

  public SimpleDeserializer(final BufferDeserializer<T> deserializer) {
    this.deserializer = deserializer;
  }

  @Override
  public void configure(final Map<String, ?> configs, final boolean isKey) {
    // Do nothing
  }

  @Override
  public T deserialize(final String topic, final byte[] bytes) {
    if (bytes == null) {
      return null;
    }
    final ReadBuffer buffer = new ReadBuffer(bytes);
    return this.deserializer.deserialize(buffer);
  }

  @Override
  public void close() {
    // Do nothing
  }

}
