package theodolite.uc4.workloadgenerator;


import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import titan.ccp.model.sensorregistry.AggregatedSensor;
import titan.ccp.model.sensorregistry.MachineSensor;
import titan.ccp.model.sensorregistry.Sensor;
import titan.ccp.model.sensorregistry.SensorRegistry;

public class SensorRegistryBuilderTest {

  @Test
  public void testStructure() {
    final SensorRegistry registry = new SensorRegistryBuilder(2, 2).build();
    final AggregatedSensor root = registry.getTopLevelSensor();
    final Collection<Sensor> firstLevelSensors = root.getChildren();
    Assert.assertEquals(2, firstLevelSensors.size());
    for (final Sensor sensor : firstLevelSensors) {
      Assert.assertTrue(sensor instanceof AggregatedSensor);
      final AggregatedSensor aggregatedSensor = (AggregatedSensor) sensor;
      final Collection<Sensor> secondLevelSensors = aggregatedSensor.getChildren();
      Assert.assertEquals(2, secondLevelSensors.size());
      for (final Sensor machineSensors : secondLevelSensors) {
        Assert.assertTrue(machineSensors instanceof MachineSensor);

      }
    }
  }

  @Test
  public void testMachineSensorNaming() {
    final SensorRegistry registry = new SensorRegistryBuilder(2, 2).build();
    final Set<String> machineSensors = registry.getMachineSensors().stream()
        .map(s -> s.getIdentifier()).collect(Collectors.toSet());

    Assert.assertTrue(machineSensors.contains("s_0"));
    Assert.assertTrue(machineSensors.contains("s_1"));
    Assert.assertTrue(machineSensors.contains("s_2"));
    Assert.assertTrue(machineSensors.contains("s_3"));
  }

}
