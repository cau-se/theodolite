package rocks.theodolite.benchmarks.loadgenerator;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
    final List<String> expectedKeySubworkload1 = IntStream
        .rangeClosed(0, 33)
        .mapToObj(id -> "prefix" + id)
        .collect(Collectors.toList());
    Assert.assertEquals(expectedKeySubworkload1, subworkload1.getKeySpace().getKeys());

    final WorkloadDefinition subworkload2 = orderedSubworkloads.get(1);
    final List<String> expectedKeySubworkload2 = IntStream
        .rangeClosed(34, 66)
        .mapToObj(id -> "prefix" + id)
        .collect(Collectors.toList());
    Assert.assertEquals(expectedKeySubworkload2, subworkload2.getKeySpace().getKeys());

    final WorkloadDefinition subworkload3 = orderedSubworkloads.get(2);
    final List<String> expectedKeySubworkload3 = IntStream
        .rangeClosed(67, 99)
        .mapToObj(id -> "prefix" + id)
        .collect(Collectors.toList());
    Assert.assertEquals(expectedKeySubworkload3, subworkload3.getKeySpace().getKeys());
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
    final List<String> expectedKeySubworkload1 = IntStream
        .rangeClosed(0, 1)
        .mapToObj(id -> "prefix" + id)
        .collect(Collectors.toList());
    Assert.assertEquals(expectedKeySubworkload1, subworkload1.getKeySpace().getKeys());

    final WorkloadDefinition subworkload2 = orderedSubworkloads.get(1);
    final List<String> expectedKeySubworkload2 = IntStream
        .rangeClosed(2, 3)
        .mapToObj(id -> "prefix" + id)
        .collect(Collectors.toList());
    Assert.assertEquals(expectedKeySubworkload2, subworkload2.getKeySpace().getKeys());

    final WorkloadDefinition subworkload3 = orderedSubworkloads.get(2);
    final List<String> expectedKeySubworkload3 = IntStream
        .rangeClosed(4, 5)
        .mapToObj(id -> "prefix" + id)
        .collect(Collectors.toList());
    Assert.assertEquals(expectedKeySubworkload3, subworkload3.getKeySpace().getKeys());

    final WorkloadDefinition subworkload4 = orderedSubworkloads.get(3);
    final List<String> expectedKeySubworkload4 = IntStream
        .rangeClosed(6, 6)
        .mapToObj(id -> "prefix" + id)
        .collect(Collectors.toList());
    Assert.assertEquals(expectedKeySubworkload4, subworkload4.getKeySpace().getKeys());

    final WorkloadDefinition subworkload5 = orderedSubworkloads.get(4);
    final List<String> expectedKeySubworkload5 = IntStream
        .rangeClosed(7, 7)
        .mapToObj(id -> "prefix" + id)
        .collect(Collectors.toList());
    Assert.assertEquals(expectedKeySubworkload5, subworkload5.getKeySpace().getKeys());

    final WorkloadDefinition subworkload6 = orderedSubworkloads.get(5);
    final List<String> expectedKeySubworkload6 = IntStream
        .rangeClosed(8, 8)
        .mapToObj(id -> "prefix" + id)
        .collect(Collectors.toList());
    Assert.assertEquals(expectedKeySubworkload6, subworkload6.getKeySpace().getKeys());

    final WorkloadDefinition subworkload7 = orderedSubworkloads.get(6);
    final List<String> expectedKeySubworkload7 = IntStream
        .rangeClosed(9, 9)
        .mapToObj(id -> "prefix" + id)
        .collect(Collectors.toList());
    Assert.assertEquals(expectedKeySubworkload7, subworkload7.getKeySpace().getKeys());
  }

  @Test
  public void testDivideWithOneEmpty() {
    final KeySpace keySpace = new KeySpace("prefix", 2);
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
    final List<String> expectedKeySubworkload1 = List.of("prefix0");
    Assert.assertEquals(expectedKeySubworkload1, subworkload1.getKeySpace().getKeys());

    final WorkloadDefinition subworkload2 = orderedSubworkloads.get(1);
    final List<String> expectedKeySubworkload2 = List.of("prefix1");
    Assert.assertEquals(expectedKeySubworkload2, subworkload2.getKeySpace().getKeys());

    final WorkloadDefinition subworkload3 = orderedSubworkloads.get(2);
    final List<String> expectedKeySubworkload3 = List.of();
    Assert.assertEquals(expectedKeySubworkload3, subworkload3.getKeySpace().getKeys());
  }

  @Test
  public void testDivideWithTwoEmpty() {
    final KeySpace keySpace = new KeySpace("prefix", 1);
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
    final List<String> expectedKeySubworkload1 = List.of("prefix0");
    Assert.assertEquals(expectedKeySubworkload1, subworkload1.getKeySpace().getKeys());

    final WorkloadDefinition subworkload2 = orderedSubworkloads.get(1);
    final List<String> expectedKeySubworkload2 = List.of();
    Assert.assertEquals(expectedKeySubworkload2, subworkload2.getKeySpace().getKeys());

    final WorkloadDefinition subworkload3 = orderedSubworkloads.get(2);
    final List<String> expectedKeySubworkload3 = List.of();
    Assert.assertEquals(expectedKeySubworkload3, subworkload3.getKeySpace().getKeys());
  }

}
