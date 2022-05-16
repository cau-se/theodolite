package rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics;

import titan.ccp.model.records.ActivePowerRecord;

/**
 * Accumulator class for AggregatedActivePowerRecords.
 */
public class AggregatedActivePowerRecordAccumulator {

  private String id;
  private long timestamp;
  private long count;
  private double sumInW;
  private double averageInW;

  /**
   * Default constructor.
   */
  public AggregatedActivePowerRecordAccumulator() {
    // This constructor is intentionally empty. Nothing special is needed here.
  }


  /**
   * Creates an AggregationObject.
   */
  public AggregatedActivePowerRecordAccumulator(final String id,
                                                final long timestamp,
                                                final long count,
                                                final double sumInW,
                                                final double averageInW) {
    this.id = id;
    this.timestamp = timestamp;
    this.count = count;
    this.sumInW = sumInW;
    this.averageInW = averageInW;
  }

  /**
   * Sets the id.
   */
  public void setId(final String id) {
    this.id = id;
  }

  /**
   * Adds the record to the aggregation.
   */
  public void addInputs(final ActivePowerRecord record) {
    this.count += 1;
    this.sumInW += record.getValueInW();
    this.timestamp = record.getTimestamp();
    this.averageInW = sumInW / count;
  }

  /**
   * Adds the records from another aggregator.
   */
  public void addInputs(final String id,
                        final double sumInW,
                        final long count,
                        final long timestamp) {
    this.id = this.id == null ? id : this.id;
    this.sumInW += sumInW;
    this.count += count;
    this.timestamp = Math.max(this.timestamp, timestamp);
    this.averageInW = this.sumInW / this.count;
  }

  /**
   * Removes the values of another aggreagator.
   * Not a complete reset since the old timestamp is lost.
   */
  public void removeInputs(final double sumInW, final long count) {
    this.sumInW -= sumInW;
    this.count -= count;
    this.averageInW = this.count == 0 ? 0.0 : this.sumInW / this.count;
    this.timestamp = -1L;
  }

  public long getCount() {
    return count;
  }

  public double getSumInW() {
    return sumInW;
  }

  public double getAverageInW() {
    return averageInW;
  }

  public String getId() {
    return id;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
