package common.functions.copy;

import common.messages.OutputMessage;
import kieker.common.record.IMonitoringRecord;

@FunctionalInterface
public interface MessageGenerator<T extends IMonitoringRecord> {

  OutputMessage<T> generateMessage(final String key);

}
