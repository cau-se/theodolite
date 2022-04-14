package rocks.theodolite.commons.model.sensorregistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Representing a real sensor in the {@link SensorRegistry}, i.e. one that is not aggregated.
 */
public interface MachineSensor extends Sensor {

  /**
   * Get a list of parent, grandparent etc. sensors.
   */
  default List<AggregatedSensor> getParents() {
    Optional<AggregatedSensor> parent = this.getParent();
    final List<AggregatedSensor> parents = new ArrayList<>();
    while (parent.isPresent()) {
      parents.add(parent.get());
      parent = parent.get().getParent();
    }
    return parents;
  }

}
