package theodolite.commons.workloadgeneration;

import java.net.URI;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * A factory for creating {@link MessageGenerator}s that creates Titan {@link ActivePowerRecord}s
 * and sends them via HTTP.
 */
public final class TitanMessageHttpGeneratorFactory {

  private final RecordSender<ActivePowerRecord> recordSender;

  private TitanMessageHttpGeneratorFactory(final RecordSender<ActivePowerRecord> recordSender) {
    this.recordSender = recordSender;
  }

  /**
   * Create a {@link MessageGenerator} that generates Titan {@link ActivePowerRecord}s with a
   * constant value.
   */
  public MessageGenerator forConstantValue(final double value) {
    return MessageGenerator.from(
        sensor -> new ActivePowerRecord(sensor, System.currentTimeMillis(), value),
        this.recordSender);
  }

  /**
   * Create a new {@link TitanMessageHttpGeneratorFactory} for the given HTTP configuration.
   */
  public static TitanMessageHttpGeneratorFactory withHttpConfig(final URI uri) {
    final HttpRecordSender<ActivePowerRecord> httpRecordSender = new HttpRecordSender<>(uri);
    return new TitanMessageHttpGeneratorFactory(httpRecordSender);
  }

}
