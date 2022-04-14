package rocks.theodolite.commons.model.sensorregistry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Optional;
import org.junit.Test;

public class MutableSensorRegistryTest {

  @Test
  public void parentOfTopLevelShouldBeNotPresent() {
    final MutableSensorRegistry sensorRegistry = new MutableSensorRegistry("root");
    final Optional<AggregatedSensor> parent = sensorRegistry.getTopLevelSensor().getParent();
    assertFalse(parent.isPresent());
  }

  @Test
  public void testEquals() {
    final MutableSensorRegistry sensorRegistry1 = this.getSensorRegistry();
    final MutableSensorRegistry sensorRegistry2 = this.getSensorRegistry();
    assertFalse(sensorRegistry1 == sensorRegistry2);
    assertTrue(sensorRegistry1.equals(sensorRegistry2));
    assertTrue(sensorRegistry2.equals(sensorRegistry1));
  }

  @Test
  public void testEqualsWithDifferentNames() {
    final MutableSensorRegistry sensorRegistry1 = this.getSensorRegistry();
    final MutableSensorRegistry sensorRegistry2 = this.getSensorRegistryWithDifferentNames();
    assertFalse(sensorRegistry1 == sensorRegistry2);
    assertTrue(sensorRegistry1.equals(sensorRegistry2));
    assertTrue(sensorRegistry2.equals(sensorRegistry1));
  }

  @Test
  public void testNotEquals() {
    final MutableSensorRegistry sensorRegistry1 = this.getSensorRegistry();
    final MutableSensorRegistry sensorRegistry2 = this.getOtherSensorRegistry();
    assertFalse(sensorRegistry1 == sensorRegistry2);
    assertFalse(sensorRegistry1.equals(sensorRegistry2));
    assertFalse(sensorRegistry2.equals(sensorRegistry1));
  }

  @Test
  public void testEqualHashCodes() {
    final MutableSensorRegistry sensorRegistry1 = this.getSensorRegistry();
    final MutableSensorRegistry sensorRegistry2 = this.getSensorRegistry();
    assertFalse(sensorRegistry1 == sensorRegistry2);
    assertTrue(sensorRegistry1.hashCode() == sensorRegistry2.hashCode());
  }

  @Test
  public void testEqualHashCodesWithDifferentNames() {
    final MutableSensorRegistry sensorRegistry1 = this.getSensorRegistry();
    final MutableSensorRegistry sensorRegistry2 = this.getSensorRegistryWithDifferentNames();
    assertFalse(sensorRegistry1 == sensorRegistry2);
    assertTrue(sensorRegistry1.hashCode() == sensorRegistry2.hashCode());
  }

  @Test
  public void testNotEqualHashCodes() {
    final MutableSensorRegistry sensorRegistry1 = this.getSensorRegistry();
    final MutableSensorRegistry sensorRegistry2 = this.getOtherSensorRegistry();
    assertFalse(sensorRegistry1 == sensorRegistry2);
    assertFalse(sensorRegistry1.hashCode() == sensorRegistry2.hashCode());
  }

  private MutableSensorRegistry getSensorRegistry() {
    final MutableSensorRegistry sensorRegistry = new MutableSensorRegistry("root", "Root");
    final MutableAggregatedSensor topLevel = sensorRegistry.getTopLevelSensor();
    topLevel.addChildMachineSensor("child-1", "Child 1");
    final MutableAggregatedSensor aggregatedSensor =
        topLevel.addChildAggregatedSensor("child-2", "Child 2");
    aggregatedSensor.addChildMachineSensor("grandchild-1", "Grandchild 1");
    aggregatedSensor.addChildMachineSensor("grandchild-2", "Grandchild 2");
    return sensorRegistry;
  }

  private MutableSensorRegistry getSensorRegistryWithDifferentNames() {
    final MutableSensorRegistry sensorRegistry = new MutableSensorRegistry("root", "Root");
    final MutableAggregatedSensor topLevel = sensorRegistry.getTopLevelSensor();
    topLevel.addChildMachineSensor("child-1", "Child 1 Alternative");
    final MutableAggregatedSensor aggregatedSensor =
        topLevel.addChildAggregatedSensor("child-2", "Child 2");
    aggregatedSensor.addChildMachineSensor("grandchild-1", "Grandchild 1");
    aggregatedSensor.addChildMachineSensor("grandchild-2", "Grandchild 2 Alternative");
    return sensorRegistry;
  }

  private MutableSensorRegistry getOtherSensorRegistry() {
    final MutableSensorRegistry sensorRegistry = new MutableSensorRegistry("root", "Root");
    final MutableAggregatedSensor topLevel = sensorRegistry.getTopLevelSensor();
    topLevel.addChildMachineSensor("child-1", "Child 1");
    final MutableAggregatedSensor aggregatedSensor =
        topLevel.addChildAggregatedSensor("child-2", "Child 2");
    aggregatedSensor.addChildMachineSensor("grandchild-1", "Grandchild 1");
    aggregatedSensor.addChildMachineSensor("grandchild-2", "Grandchild 2");
    aggregatedSensor.addChildMachineSensor("grandchild-3", "Grandchild 3");
    return sensorRegistry;
  }

}
