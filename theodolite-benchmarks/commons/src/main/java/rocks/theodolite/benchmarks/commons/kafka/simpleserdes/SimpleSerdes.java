package rocks.theodolite.benchmarks.commons.kafka.simpleserdes;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

/**
 * Factory class to create <i>Simple Serdes</i>. These are serdes created from a
 * {@link BufferSerializer} and a {@link BufferDeserializer}.
 */
public final class SimpleSerdes {

  private SimpleSerdes() {}

  public static <T> Serde<T> create(final BufferSerde<T> serde) {
    return Serdes.serdeFrom(new SimpleSerializer<>(serde), new SimpleDeserializer<>(serde));
  }

  public static <T> Serde<T> create(final BufferSerializer<T> serializer,
      final BufferDeserializer<T> deserializer) {
    return Serdes.serdeFrom(new SimpleSerializer<>(serializer),
        new SimpleDeserializer<>(deserializer));
  }

}
