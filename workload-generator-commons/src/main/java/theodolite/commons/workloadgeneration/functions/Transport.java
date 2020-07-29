package theodolite.commons.workloadgeneration.functions;

import kieker.common.record.IMonitoringRecord;

/**
 * This interface describes a function that consumes a {@link IMonitoringRecord}. This function is
 * dedicated to be used to transport individual messages to the messaging system.
 *
 * @param <T> the type of records to send as messages.
 */
@FunctionalInterface
public interface Transport<T extends IMonitoringRecord> {

  void transport(final T message);

}
