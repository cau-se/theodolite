package rocks.theodolite.benchmarks.uc3.beam;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;

/**
 * Maps the key of incoming records from pure sensor ID strings to {@link HourOfDayKey}s.
 */
public class MapTimeFormat
    extends SimpleFunction<KV<String, ActivePowerRecord>, KV<HourOfDayKey, ActivePowerRecord>> {
  private static final long serialVersionUID = -6597391279968647035L;
  private final StatsKeyFactory<HourOfDayKey> keyFactory = new HourOfDayKeyFactory();
  private final ZoneId zone = ZoneId.of("Europe/Paris");

  @Override
  public KV<HourOfDayKey, ActivePowerRecord> apply(final KV<String, ActivePowerRecord> kv) {
    final Instant instant = Instant.ofEpochMilli(kv.getValue().getTimestamp());
    final LocalDateTime dateTime = LocalDateTime.ofInstant(instant, this.zone);
    return KV.of(
        this.keyFactory.createKey(kv.getValue().getIdentifier(), dateTime),
        kv.getValue());
  }
}
