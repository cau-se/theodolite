package theodolite.commons.workloadgeneration;

import java.io.Serializable;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WorkloadDefinition implements Serializable {

  private static final long serialVersionUID = -8337364281221817001L; // NOPMD

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
    final int effParts = Math.min(parts, this.keySpace.getCount());
    final int minSize = this.keySpace.getCount() / effParts;
    final int largerParts = this.keySpace.getCount() % effParts;
    return IntStream.range(0, effParts)
        .mapToObj(part -> {
          final int thisSize = part < largerParts ? minSize + 1 : minSize;
          final int largePartsBefore = Math.min(largerParts, part);
          final int smallPartsBefore = part - largePartsBefore;
          final int start = largePartsBefore * (minSize + 1) + smallPartsBefore * minSize;
          final int end = start + thisSize - 1;
          return new KeySpace(
              this.keySpace.getPrefix(),
              start,
              end);
        })
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
