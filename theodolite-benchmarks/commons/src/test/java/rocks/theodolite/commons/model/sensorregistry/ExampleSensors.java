package rocks.theodolite.commons.model.sensorregistry;

import java.util.List;
import java.util.stream.Collectors;

public final class ExampleSensors {

  private static final SensorRegistry REGISTRY;

  static {
    final MutableSensorRegistry sensorRegistry = new MutableSensorRegistry("root");
    final MutableAggregatedSensor topLevel = sensorRegistry.getTopLevelSensor();
    final MutableAggregatedSensor comcent = topLevel.addChildAggregatedSensor("comcent");
    final MutableAggregatedSensor server1 = comcent.addChildAggregatedSensor("comcent.server1");
    final MachineSensor server1pw1 = server1.addChildMachineSensor("comcent.server1.pw1");
    final MachineSensor server1pw2 = server1.addChildMachineSensor("comcent.server1.pw2");
    final MachineSensor server1pw3 = server1.addChildMachineSensor("comcent.server1.pw3");
    final MutableAggregatedSensor server2 = comcent.addChildAggregatedSensor("comcent.server2");
    final MachineSensor server2pw1 = server2.addChildMachineSensor("comcent.server2.pw1");
    final MachineSensor server2pw2 = server2.addChildMachineSensor("comcent.server2.pw2");

    REGISTRY = ImmutableSensorRegistry.copyOf(sensorRegistry);
  }

  private ExampleSensors() {}

  public static List<String> machineSensorNames() {
    return REGISTRY.getMachineSensors().stream().map(s -> s.getIdentifier())
        .collect(Collectors.toList());
  }

  public static SensorRegistry registry() {
    return REGISTRY;
  }

}
