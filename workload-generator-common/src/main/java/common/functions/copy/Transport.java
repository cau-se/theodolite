package common.functions.copy;

import common.messages.OutputMessage;
import kieker.common.record.IMonitoringRecord;

@FunctionalInterface
public interface Transport<T extends IMonitoringRecord> {
  
  public void transport(final OutputMessage<T> message);

}
