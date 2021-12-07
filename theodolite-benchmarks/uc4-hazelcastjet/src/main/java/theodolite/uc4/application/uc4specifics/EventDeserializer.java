package theodolite.uc4.application.uc4specifics;

import java.util.Map;
import org.apache.kafka.common.serialization.ByteBufferDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import titan.ccp.configuration.events.Event;

public class EventDeserializer implements Deserializer<Event> {

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
