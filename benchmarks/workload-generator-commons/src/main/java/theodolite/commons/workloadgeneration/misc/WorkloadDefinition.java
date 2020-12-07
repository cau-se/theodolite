package theodolite.commons.workloadgeneration.misc;

import theodolite.commons.workloadgeneration.dimensions.KeySpace;

/**
 * The central class that contains all information that needs to be exchanged between the nodes for
 * distributed workload generation.
 */
public class WorkloadDefinition {
  private static final int ZERO = 0;
  private static final int ONE = 1;
  private static final int TWO = 2;
  private static final int THREE = 3;
  private static final int FOUR = 4;

  private final KeySpace keySpace;
  private final int numberOfWorkers;

  /**
   * Create a new workload definition.
   *
   * @param keySpace the key space to use.
   * @param numberOfWorkers the number of workers participating in the workload generation.
   */
  public WorkloadDefinition(final KeySpace keySpace, final int numberOfWorkers) {

    this.keySpace = keySpace;
    this.numberOfWorkers = numberOfWorkers;
  }

  public KeySpace getKeySpace() {
    return this.keySpace;
  }

  public int getNumberOfWorkers() {
    return this.numberOfWorkers;
  }

  /**
   * Simple method for encoding all information of the workload definition into one string.
   *
   * @return a string that encodes all information of the workload generation in a compact format.
   *         The format is 'keySpace;keySpace.min;keySpace.max;numberOfWorkers'.
   */
  @Override
  public String toString() {
    return this.getKeySpace().getPrefix() + ";" + this.getKeySpace().getMin() + ";"
        + this.getKeySpace().getMax() + ";" + this.getNumberOfWorkers();
  }

  /**
   * Parse a workload generation from a previously encoded string with the format returned by
   * {@link WorkloadDefinition#toString()}.
   *
   * @param workloadDefinitionString the workload definition string.
   * @return the parsed workload definition.
   */
  public static WorkloadDefinition fromString(final String workloadDefinitionString) {
    final String[] deserialized = workloadDefinitionString.split(";");

    if (deserialized.length != FOUR) {
      throw new IllegalArgumentException(
          "Wrong workload definition string when trying to parse the workload generation.");
    }

    return new WorkloadDefinition(
        new KeySpace(deserialized[ZERO], Integer.valueOf(deserialized[ONE]),
            Integer.valueOf(deserialized[TWO])),
        Integer.valueOf(deserialized[THREE]));
  }
}
