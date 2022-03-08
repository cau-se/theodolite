package rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics;

import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import titan.ccp.configuration.events.Event;
import titan.ccp.configuration.events.EventSerde;

/**
 * Deserializer for Event Objects.
 *
 */
public class EventDeserializer implements Deserializer<Event> {

  private final Deserializer<Event> deserializer = EventSerde.serde().deserializer();

  @Override
  public void configure(final Map<String, ?> configs, final boolean isKey) {
    this.deserializer.configure(configs, isKey);
  }

  @Override
  public Event deserialize(final String topic, final byte[] data) {
    return this.deserializer.deserialize(topic, data);
  }

  @Override
  public void close() {
    this.deserializer.close();
  }


}
