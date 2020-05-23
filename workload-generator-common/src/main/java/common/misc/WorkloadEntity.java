package common.misc;

import common.functions.MessageGenerator;
import kieker.common.record.IMonitoringRecord;

public class WorkloadEntity<T extends IMonitoringRecord> {
  private final String key;
  private final MessageGenerator<T> generator;

  public WorkloadEntity(final String key, final MessageGenerator<T> generator) {
    this.key = key;
    this.generator = generator;
  }

  public T generateMessage() {
    return this.generator.generateMessage(this.key);
  }
}
