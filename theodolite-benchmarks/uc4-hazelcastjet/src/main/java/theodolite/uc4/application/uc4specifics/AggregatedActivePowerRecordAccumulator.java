package theodolite.uc4.application.uc4specifics;

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
