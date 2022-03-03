package rocks.theodolite.benchmarks.loadgenerator;

import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A set of keys, where each key consists of a prefix and a number.
 */
public class KeySpace implements Serializable {

  private static final long serialVersionUID = 7343135392720315516L; // NOPMD

  private final String prefix;
  private final int min;
  private final int max;

  /**
   * Create a new key space. All keys will have the prefix {@code prefix}. The remaining part of
   * each key will be determined by a number of the interval [{@code min}, {@code max}).
   *
   * @param prefix the prefix to use for all keys
   * @param min the lower bound (inclusive) to start counting from
   * @param max the upper bound (exclusive) to count to
   */
  public KeySpace(final String prefix, final int min, final int max) {
    this.prefix = prefix;
    this.min = min;
    this.max = max;
  }

  public KeySpace(final String prefix, final int numberOfKeys) {
    this(prefix, 0, numberOfKeys);
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

  /**
   * Get the amount of keys in this {@link KeySpace}.
   */
  public int getCount() {
    return this.max - this.min;
  }

  /**
   * Get all keys in this {@link KeySpace}.
   */
  public Collection<String> getKeys() {
    return IntStream.range(this.min, this.max)
        .mapToObj(id -> this.prefix + id)
        .collect(Collectors.toUnmodifiableList());
  }

  @Override
  public String toString() {
    return this.prefix + '[' + this.min + '-' + this.max + ')';
  }

}
