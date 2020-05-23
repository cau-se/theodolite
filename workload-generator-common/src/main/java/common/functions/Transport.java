package common.functions;

import kieker.common.record.IMonitoringRecord;

@FunctionalInterface
public interface Transport<T extends IMonitoringRecord> {

  void transport(final T message);

}
