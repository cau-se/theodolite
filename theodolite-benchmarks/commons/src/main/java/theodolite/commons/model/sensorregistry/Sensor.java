package theodolite.commons.model.sensorregistry;

import java.util.Optional;

/**
 * Class representing a sensor in the {@link SensorRegistry}.
 */
public interface Sensor {

  Optional<AggregatedSensor> getParent();

  String getIdentifier();

  String getName();

}
