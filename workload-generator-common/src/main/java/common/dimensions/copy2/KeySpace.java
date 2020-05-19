package common.dimensions.copy2;

import common.generators.WorkloadGenerator;

/**
 * Wrapper class for the definition of the Keys that should be used by the
 * {@link WorkloadGenerator}.
 */
public class KeySpace extends Dimension {

  private final String prefix;
  private final int min;
  private final int max;


  /**
   * Create a new key space. All keys will have the prefix {@code prefix}. The remaining part of
   * each key will be determined by a number of the interval ({@code min}, {@code max}-1).
   *
   * @param prefix the prefix to use for all keys
   * @param min the lower bound (inclusive) to start counting from
   * @param max the upper bound (exclusive) to count to
   */
  public KeySpace(final String prefix, final int min, final int max) {
    if (prefix == null || prefix.contains(";")) {
      throw new IllegalArgumentException(
          "The prefix must not be null and must not contain the ';' character.");
    }
    this.prefix = prefix;
    this.min = min;
    this.max = max;

  }

  public KeySpace(final String prefix, final int numberOfKeys) {
    this(prefix, 0, numberOfKeys - 1);
  }

  public KeySpace(final int numberOfKeys) {
    this("sensor_", 0, numberOfKeys - 1);
  }

  public String getPrefix() {
    return this.prefix;
  }


  public int getMin() {
    return this.min;
  }


  public int getMax() {
    return this.max;
  }
}
