package common.messages.copy2;

import kieker.common.record.IMonitoringRecord;

/*
 * Wrapper class for messages within the messaging system.
 */
public class OutputMessage<T extends IMonitoringRecord> {
  private final String key;
  private final T value;

  /***
   * Create a new Message.
   * 
   * @param key the key of the message.
   * @param value the value of the message.
   */
  public OutputMessage(final String key, final T value) {
    super();
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return this.key;
  }

  public T getValue() {
    return this.value;
  }

}
