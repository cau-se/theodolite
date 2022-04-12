package theodolite.commons.configuration.events;

import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.kafka.common.serialization.ByteBufferDeserializer;
import org.apache.kafka.common.serialization.ByteBufferSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Provides factory methods for creating Kafka serializers and deserializers for {@link Event}s.
 */
public final class EventSerde {

  private EventSerde() {}

  public static Serde<Event> serde() {
    return Serdes.serdeFrom(new EventSerializer(), new EventDeserializer());
  }

  public static Serializer<Event> serializer() {
    return new EventSerializer();
  }

  public static Deserializer<Event> deserializer() {
    return new EventDeserializer();
  }

  private static class EventSerializer implements Serializer<Event> {

    private static final int INT_SIZE = 4;

    private final ByteBufferSerializer byteBufferSerializer = new ByteBufferSerializer();

    @Override
    public void configure(final Map<String, ?> configs, final boolean isKey) {
      this.byteBufferSerializer.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(final String topic, final Event event) {
      return this.byteBufferSerializer.serialize(topic,
          ByteBuffer.allocate(INT_SIZE).putInt(event.ordinal()));
    }

    @Override
    public void close() {
      this.byteBufferSerializer.close();
    }

  }

  private static class EventDeserializer implements Deserializer<Event> {

    private final ByteBufferDeserializer byteBufferDeserializer = new ByteBufferDeserializer();

    @Override
    public void configure(final Map<String, ?> configs, final boolean isKey) {
      this.byteBufferDeserializer.configure(configs, isKey);
    }

    @Override
    public Event deserialize(final String topic, final byte[] data) {
      final int ordinal = this.byteBufferDeserializer.deserialize(topic, data).getInt();
      for (final Event event : Event.values()) {
        if (ordinal == event.ordinal()) {
          return event;
        }
      }
      throw new IllegalArgumentException("Deserialized data is not a valid event.");
    }

    @Override
    public void close() {
      this.byteBufferDeserializer.close();
    }

  }
}
