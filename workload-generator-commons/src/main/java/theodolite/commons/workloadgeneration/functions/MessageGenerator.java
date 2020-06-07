package theodolite.commons.workloadgeneration.functions;

import kieker.common.record.IMonitoringRecord;

@FunctionalInterface
public interface MessageGenerator<T extends IMonitoringRecord> {

  T generateMessage(final String key);

}
