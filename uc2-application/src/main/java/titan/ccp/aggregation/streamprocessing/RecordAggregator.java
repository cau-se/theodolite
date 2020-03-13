package titan.ccp.aggregation.streamprocessing;

import org.apache.kafka.streams.kstream.Windowed;
import titan.ccp.models.records.ActivePowerRecord;
import titan.ccp.models.records.AggregatedActivePowerRecord;

/**
 * Updates an {@link AggregatedActivePowerRecord} by a new {@link ActivePowerRecord}.
 */
public class RecordAggregator {

  /**
   * Adds an {@link ActivePowerRecord} to an {@link AggregatedActivePowerRecord}.
   */
  public AggregatedActivePowerRecord add(final Windowed<String> identifier,
      final ActivePowerRecord record, final AggregatedActivePowerRecord aggregated) {
    final long count = (aggregated == null ? 0 : aggregated.getCount()) + 1;
    final double sum = (aggregated == null ? 0.0 : aggregated.getSumInW()) + record.getValueInW();
    final double average = count == 0 ? 0.0 : sum / count;
    return new AggregatedActivePowerRecord(
        identifier.key(), record.getTimestamp(),
        0.0, 0.0, count, sum, average);
  }

  /**
   * Substracts an {@link ActivePowerRecord} to an {@link AggregatedActivePowerRecord}.
   */
  public AggregatedActivePowerRecord substract(final Windowed<String> identifier,
      final ActivePowerRecord record, final AggregatedActivePowerRecord aggregated) {
    final long count = aggregated.getCount() - 1;
    final double sum = aggregated.getSumInW() - record.getValueInW();
    final double average = count == 0 ? 0.0 : sum / count;
    return new AggregatedActivePowerRecord(
        // TODO timestamp -1 indicates that this record is emitted by an substract event
        identifier.key(), -1,
        0.0, 0.0, count, sum, average);
  }

}
