package rocks.theodolite.benchmarks.uc3.beam;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Changes the time format to us Europe/Paris time.
 */
public class MapTimeFormat
    extends SimpleFunction<ActivePowerRecord, KV<HourOfDayKey, ActivePowerRecord>> {
  private static final long serialVersionUID = -6597391279968647035L;
  private final StatsKeyFactory<HourOfDayKey> keyFactory = new HourOfDayKeyFactory();
  private final ZoneId zone = ZoneId.of("Europe/Paris");

  @Override
  public KV<HourOfDayKey, ActivePowerRecord> apply(final ActivePowerRecord record) {
    final Instant instant = Instant.ofEpochMilli(record.getTimestamp());
    final LocalDateTime dateTime = LocalDateTime.ofInstant(instant, this.zone);
    return KV.of(
        this.keyFactory.createKey(record.getIdentifier(), dateTime),
        record);
  }
}
