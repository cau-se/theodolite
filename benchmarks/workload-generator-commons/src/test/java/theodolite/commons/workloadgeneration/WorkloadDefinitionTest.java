package theodolite.commons.workloadgeneration;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class WorkloadDefinitionTest {

  @Test
  public void testDivideByOneAmount() {
    final KeySpace keySpace = new KeySpace("prefix", 100);
    final WorkloadDefinition workload = new WorkloadDefinition(keySpace, Duration.ofSeconds(1));
    final Set<WorkloadDefinition> subworkloads = workload.divide(1);
    Assert.assertEquals(1, subworkloads.size());
  }

  @Test
  public void testDivideMultipleAmount() {
    final KeySpace keySpace = new KeySpace("prefix", 100);
    final WorkloadDefinition workload = new WorkloadDefinition(keySpace, Duration.ofSeconds(1));
    final Set<WorkloadDefinition> subworkloads = workload.divide(2);
    Assert.assertEquals(2, subworkloads.size());
  }

  @Test
  public void testDivideNonMultipleAmount() {
    final KeySpace keySpace = new KeySpace("prefix", 100);
    final WorkloadDefinition workload = new WorkloadDefinition(keySpace, Duration.ofSeconds(1));
    final Set<WorkloadDefinition> subworkloads = workload.divide(3);
    Assert.assertEquals(3, subworkloads.size());
  }

  @Test
  public void testDivide() {
    final KeySpace keySpace = new KeySpace("prefix", 100);
    final WorkloadDefinition workload = new WorkloadDefinition(keySpace, Duration.ofSeconds(1));
    final Set<WorkloadDefinition> subworkloads = workload.divide(3);
    Assert.assertEquals(3, subworkloads.size());
    for (final WorkloadDefinition subworkload : subworkloads) {
      Assert.assertEquals("prefix", subworkload.getKeySpace().getPrefix());
      Assert.assertEquals(Duration.ofSeconds(1), subworkload.getPeriod());
    }
    final List<WorkloadDefinition> orderedSubworkloads = subworkloads.stream()
        .sorted(Comparator.comparingInt(l -> l.getKeySpace().getMin()))
        .collect(Collectors.toList());
    final WorkloadDefinition subworkload1 = orderedSubworkloads.get(0);
    Assert.assertEquals(0, subworkload1.getKeySpace().getMin());
    Assert.assertEquals(33, subworkload1.getKeySpace().getMax());
    final WorkloadDefinition subworkload2 = orderedSubworkloads.get(1);
    Assert.assertEquals(34, subworkload2.getKeySpace().getMin());
    Assert.assertEquals(66, subworkload2.getKeySpace().getMax());
    final WorkloadDefinition subworkload3 = orderedSubworkloads.get(2);
    Assert.assertEquals(67, subworkload3.getKeySpace().getMin());
    Assert.assertEquals(99, subworkload3.getKeySpace().getMax());
  }

  @Test
  public void testDivideMany() {
    final KeySpace keySpace = new KeySpace("prefix", 10);
    final WorkloadDefinition workload = new WorkloadDefinition(keySpace, Duration.ofSeconds(1));
    final Set<WorkloadDefinition> subworkloads = workload.divide(7);
    Assert.assertEquals(7, subworkloads.size());
    for (final WorkloadDefinition subworkload : subworkloads) {
      Assert.assertEquals("prefix", subworkload.getKeySpace().getPrefix());
      Assert.assertEquals(Duration.ofSeconds(1), subworkload.getPeriod());
    }
    final List<WorkloadDefinition> orderedSubworkloads = subworkloads.stream()
        .sorted(Comparator.comparingInt(l -> l.getKeySpace().getMin()))
        .collect(Collectors.toList());
    final WorkloadDefinition subworkload1 = orderedSubworkloads.get(0);
    Assert.assertEquals(0, subworkload1.getKeySpace().getMin());
    Assert.assertEquals(1, subworkload1.getKeySpace().getMax());
    final WorkloadDefinition subworkload2 = orderedSubworkloads.get(1);
    Assert.assertEquals(2, subworkload2.getKeySpace().getMin());
    Assert.assertEquals(3, subworkload2.getKeySpace().getMax());
    final WorkloadDefinition subworkload3 = orderedSubworkloads.get(2);
    Assert.assertEquals(4, subworkload3.getKeySpace().getMin());
    Assert.assertEquals(5, subworkload3.getKeySpace().getMax());
    final WorkloadDefinition subworkload4 = orderedSubworkloads.get(3);
    Assert.assertEquals(6, subworkload4.getKeySpace().getMin());
    Assert.assertEquals(6, subworkload4.getKeySpace().getMax());
    final WorkloadDefinition subworkload5 = orderedSubworkloads.get(4);
    Assert.assertEquals(7, subworkload5.getKeySpace().getMin());
    Assert.assertEquals(7, subworkload5.getKeySpace().getMax());
    final WorkloadDefinition subworkload6 = orderedSubworkloads.get(5);
    Assert.assertEquals(8, subworkload6.getKeySpace().getMin());
    Assert.assertEquals(8, subworkload6.getKeySpace().getMax());
    final WorkloadDefinition subworkload7 = orderedSubworkloads.get(6);
    Assert.assertEquals(9, subworkload7.getKeySpace().getMin());
    Assert.assertEquals(9, subworkload7.getKeySpace().getMax());
  }

}
