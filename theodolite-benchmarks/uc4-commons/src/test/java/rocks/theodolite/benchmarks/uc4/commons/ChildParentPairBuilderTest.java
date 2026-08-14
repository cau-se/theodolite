package rocks.theodolite.benchmarks.uc4.commons;

import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.MutableAggregatedSensor;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.MutableSensorRegistry;

public class ChildParentPairBuilderTest {

  @Test
  public void buildsPairsForDirectAndNestedSensors() {
    final MutableSensorRegistry registry = new MutableSensorRegistry("root");
    final MutableAggregatedSensor group =
        registry.getTopLevelSensor().addChildAggregatedSensor("group");
    final MutableAggregatedSensor nestedGroup = group.addChildAggregatedSensor("nested-group");
    group.addChildMachineSensor("sensor");
    nestedGroup.addChildMachineSensor("nested-sensor");

    final Map<String, Set<String>> pairs = ChildParentPairBuilder.build(registry);

    Assert.assertEquals(Map.of(
        "group", Set.of("root"),
        "nested-group", Set.of("group"),
        "sensor", Set.of("group"),
        "nested-sensor", Set.of("nested-group")), pairs);
  }

  @Test
  public void excludesTopLevelSensor() {
    final MutableSensorRegistry registry = new MutableSensorRegistry("root");

    Assert.assertTrue(ChildParentPairBuilder.build(registry).isEmpty());
  }

}