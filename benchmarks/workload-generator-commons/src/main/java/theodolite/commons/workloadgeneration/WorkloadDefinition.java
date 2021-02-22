package theodolite.commons.workloadgeneration;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WorkloadDefinition {

  private final KeySpace keySpace;
  private final Duration period;

  /**
   * Create a new workload definition.
   *
   * @param keySpace the key space to use.
   */
  public WorkloadDefinition(
      final KeySpace keySpace,
      final Duration period) {
    this.keySpace = keySpace;
    this.period = period;
  }

  public KeySpace getKeySpace() {
    return this.keySpace;
  }

  public Duration getPeriod() {
    return this.period;
  }

  public Set<WorkloadDefinition> divide(final int parts) {
    final int size = (this.keySpace.getCount() + parts - 1) / parts; // = ceil(count/parts)
    return IntStream.range(0, parts)
        .mapToObj(part -> new KeySpace(
            this.keySpace.getPrefix(),
            this.keySpace.getMin() + part * size,
            Math.min(this.keySpace.getMin() + (part + 1) * size - 1, this.keySpace.getMax())))
        .map(keySpace -> new WorkloadDefinition(
            keySpace,
            this.period))
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public String toString() {
    return this.keySpace + ";" + this.period.toMillis();
  }

}
