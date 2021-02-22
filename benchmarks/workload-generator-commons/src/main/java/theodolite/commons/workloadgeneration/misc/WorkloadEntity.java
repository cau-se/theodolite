package theodolite.commons.workloadgeneration.misc;

import theodolite.commons.workloadgeneration.functions.RecordGenerator;

/**
 * Representation of a entity of the workload generation that generates load for one fixed key.
 *
 * @param <T> The type of records the workload generator is dedicated for.
 */
public class WorkloadEntity<T> {
  private final String key;
  private final RecordGenerator<T> generator;

  public WorkloadEntity(final String key, final RecordGenerator<T> generator) {
    this.key = key;
    this.generator = generator;
  }

  public T generateMessage() {
    return this.generator.generate(this.key);
  }
}
