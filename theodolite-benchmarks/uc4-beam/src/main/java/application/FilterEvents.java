package application;

import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import titan.ccp.configuration.events.Event;

/**
 * Filters for {@code Event.SENSOR_REGISTRY_CHANGED} and
 * {@code Event.SENSOR_REGISTRY_STATUS} events.
 */
public class FilterEvents implements SerializableFunction<KV<Event, String>, Boolean> {
  private static final long serialVersionUID = -2233447357614891559L;

  @Override
  public Boolean apply(final KV<Event, String> kv) {
    return kv.getKey() == Event.SENSOR_REGISTRY_CHANGED
        || kv.getKey() == Event.SENSOR_REGISTRY_STATUS;
  }
}
