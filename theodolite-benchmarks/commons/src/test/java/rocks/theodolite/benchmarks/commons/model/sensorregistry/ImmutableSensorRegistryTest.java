package rocks.theodolite.benchmarks.commons.model.sensorregistry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import rocks.theodolite.benchmarks.commons.model.sensorregistry.ImmutableSensorRegistry;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.MutableAggregatedSensor;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.MutableSensorRegistry;

public class ImmutableSensorRegistryTest {

  @Test
  public void testEquals() {
    final ImmutableSensorRegistry sensorRegistry1 =
        ImmutableSensorRegistry.copyOf(this.getSensorRegistry());
    final ImmutableSensorRegistry sensorRegistry2 =
        ImmutableSensorRegistry.copyOf(this.getSensorRegistry());
    assertFalse(sensorRegistry1 == sensorRegistry2);
    assertTrue(sensorRegistry1.equals(sensorRegistry2));
    assertTrue(sensorRegistry2.equals(sensorRegistry1));
  }

  @Test
  public void testEqualsWithDifferentNames() {
    final ImmutableSensorRegistry sensorRegistry1 =
        ImmutableSensorRegistry.copyOf(this.getSensorRegistry());
    final ImmutableSensorRegistry sensorRegistry2 =
        ImmutableSensorRegistry.copyOf(this.getSensorRegistryWithDifferentNames());
    assertFalse(sensorRegistry1 == sensorRegistry2);
    assertTrue(sensorRegistry1.equals(sensorRegistry2));
    assertTrue(sensorRegistry2.equals(sensorRegistry1));
  }

  @Test
  public void testNotEquals() {
    final ImmutableSensorRegistry sensorRegistry1 =
        ImmutableSensorRegistry.copyOf(this.getSensorRegistry());
    final ImmutableSensorRegistry sensorRegistry2 =
        ImmutableSensorRegistry.copyOf(this.getOtherSensorRegistry());
    assertFalse(sensorRegistry1 == sensorRegistry2);
    assertFalse(sensorRegistry1.equals(sensorRegistry2));
    assertFalse(sensorRegistry2.equals(sensorRegistry1));
  }

  @Test
  public void testEqualHashCodes() {
    final ImmutableSensorRegistry sensorRegistry1 =
        ImmutableSensorRegistry.copyOf(this.getSensorRegistry());
    final ImmutableSensorRegistry sensorRegistry2 =
        ImmutableSensorRegistry.copyOf(this.getSensorRegistry());
    assertFalse(sensorRegistry1 == sensorRegistry2);
    assertTrue(sensorRegistry1.hashCode() == sensorRegistry2.hashCode());
  }

  @Test
  public void testEqualHashCodesWithDifferentNames() {
    final ImmutableSensorRegistry sensorRegistry1 =
        ImmutableSensorRegistry.copyOf(this.getSensorRegistry());
    final ImmutableSensorRegistry sensorRegistry2 =
        ImmutableSensorRegistry.copyOf(this.getSensorRegistryWithDifferentNames());
    assertFalse(sensorRegistry1 == sensorRegistry2);
    assertTrue(sensorRegistry1.hashCode() == sensorRegistry2.hashCode());
  }

  @Test
  public void testNotEqualHashCodes() {
    final ImmutableSensorRegistry sensorRegistry1 =
        ImmutableSensorRegistry.copyOf(this.getSensorRegistry());
    final ImmutableSensorRegistry sensorRegistry2 =
        ImmutableSensorRegistry.copyOf(this.getOtherSensorRegistry());
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
