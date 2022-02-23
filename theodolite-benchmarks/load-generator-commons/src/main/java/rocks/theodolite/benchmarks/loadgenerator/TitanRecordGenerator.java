package rocks.theodolite.benchmarks.loadgenerator;

import java.time.Clock;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * A factory for creating {@link RecordGenerator}s that creates Titan {@link ActivePowerRecord}s.
 */
public final class TitanRecordGenerator implements RecordGenerator<ActivePowerRecord> {

  private final Clock clock;

  private final double constantValue;

  private TitanRecordGenerator(final double constantValue) {
    this.constantValue = constantValue;
    this.clock = Clock.systemUTC();
  }

  /* default */ TitanRecordGenerator(final double constantValue, final Clock clock) {
    this.constantValue = constantValue;
    this.clock = clock;
  }

  /**
   * Create a {@link RecordGenerator} that generates Titan {@link ActivePowerRecord}s with a
   * constant value.
   */
  public static RecordGenerator<ActivePowerRecord> forConstantValue(final double value) {
    return new TitanRecordGenerator(value);
  }

  @Override
  public ActivePowerRecord generate(final String key) {
    return new ActivePowerRecord(key, this.clock.millis(), this.constantValue);
  }

}
