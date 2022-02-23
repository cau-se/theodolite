package rocks.theodolite.benchmarks.uc4.loadgenerator;

import titan.ccp.model.sensorregistry.MutableAggregatedSensor;
import titan.ccp.model.sensorregistry.MutableSensorRegistry;
import titan.ccp.model.sensorregistry.SensorRegistry;

/**
 * Builder for creating a nested {@link SensorRegistry} with {@code numNestedGroups} levels and
 * {@code numSensors} children per group.
 */
public final class SensorRegistryBuilder {

  private final int numNestedGroups;
  private final int numSensors;

  public SensorRegistryBuilder(final int numNestedGroups, final int numSensors) {
    this.numNestedGroups = numNestedGroups;
    this.numSensors = numSensors;
  }

  /**
   * Creates the {@link SensorRegistry}.
   */
  public SensorRegistry build() {
    final MutableSensorRegistry sensorRegistry = new MutableSensorRegistry("group_lvl_0");
    this.addChildren(
        sensorRegistry.getTopLevelSensor(),
        this.numSensors,
        1,
        this.numNestedGroups,
        0);
    return sensorRegistry;
  }

  private int addChildren(final MutableAggregatedSensor parent, final int numChildren,
      final int lvl, final int maxLvl, final int startId) {
    int nextId = startId;
    for (int c = 0; c < numChildren; c++) {
      if (lvl == maxLvl) {
        parent.addChildMachineSensor("s_" + nextId);
        nextId++;
      } else {
        final MutableAggregatedSensor newParent =
            parent.addChildAggregatedSensor("g_" + lvl + '_' + nextId);
        nextId = this.addChildren(newParent, numChildren, lvl + 1, maxLvl, nextId);
      }
    }
    return nextId;
  }

}
