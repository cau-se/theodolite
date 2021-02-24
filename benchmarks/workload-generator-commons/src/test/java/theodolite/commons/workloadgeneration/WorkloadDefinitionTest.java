package theodolite.commons.workloadgeneration.misc;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import theodolite.commons.workloadgeneration.KeySpace;
import theodolite.commons.workloadgeneration.WorkloadDefinition;

public class WorkloadDefinition2Test {

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
    Assert.assertEquals(67, subworkload2.getKeySpace().getMax());
    final WorkloadDefinition subworkload3 = orderedSubworkloads.get(2);
    Assert.assertEquals(68, subworkload3.getKeySpace().getMin());
    Assert.assertEquals(99, subworkload3.getKeySpace().getMax());
  }

}
