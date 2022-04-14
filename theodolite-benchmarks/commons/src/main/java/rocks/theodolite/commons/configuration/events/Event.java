package rocks.theodolite.commons.configuration.events;

import rocks.theodolite.commons.model.sensorregistry.SensorRegistry;

/**
 * Events that could occur when modifying a {@link SensorRegistry}. Currently only a general change
 * event and a status posting event are supported.
 */
public enum Event {

  SENSOR_REGISTRY_CHANGED, SENSOR_REGISTRY_STATUS;

}
