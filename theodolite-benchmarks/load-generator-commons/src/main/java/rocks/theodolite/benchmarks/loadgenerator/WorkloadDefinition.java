package rocks.theodolite.benchmarks.loadgenerator;

import java.io.Serializable;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Definition of a workload consisting of a {@link KeySpace} and a period with which messages will
 * be generated for key of that {@link KeySpace}.
 */
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

  /**
   * Divide this {@link WorkloadDefinition} into {@code parts} {@link WorkloadDefinition}s by
   * distributing its {@link KeySpace} (almost) equally among all {@link WorkloadDefinition}s.
   */
  public Set<WorkloadDefinition> divide(final int parts) {
    // final int effParts = Math.min(parts, this.keySpace.getCount());
    final int minSize = this.keySpace.getCount() / parts;
    final int largerParts = this.keySpace.getCount() % parts;
    return IntStream.range(0, parts)
        .mapToObj(part -> {
          final int thisSize = part < largerParts ? minSize + 1 : minSize;
          final int largePartsBefore = Math.min(largerParts, part);
          final int smallPartsBefore = part - largePartsBefore;
          final int start = largePartsBefore * (minSize + 1) + smallPartsBefore * minSize;
          final int end = start + thisSize;
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
