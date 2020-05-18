package common;

import common.functions.MessageGenerator;
import common.messages.OutputMessage;

public class WorkloadEntity {
  private final String key;
  private final MessageGenerator generator;
  
  public WorkloadEntity(final String key, final MessageGenerator generator) {
    this.key = key;
    this.generator = generator;
  }
  
  public OutputMessage generateMessage() {
    return this.generator.generateMessage(this.key);
  }
}
