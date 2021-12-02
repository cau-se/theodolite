package theodolite.commons.workloadgeneration;

import titan.ccp.model.records.ActivePowerRecord;

/**
 * A factory for creating {@link RecordGenerator}s that creates Titan {@link ActivePowerRecord}s.
 */
public final class TitanRecordGeneratorFactory {


  private TitanRecordGeneratorFactory() {}

  /**
   * Create a {@link RecordGenerator} that generates Titan {@link ActivePowerRecord}s with a
   * constant value.
   */
  public static RecordGenerator<ActivePowerRecord> forConstantValue(final double value) {
    return sensor -> new ActivePowerRecord(sensor, System.currentTimeMillis(), value);
  }

}
