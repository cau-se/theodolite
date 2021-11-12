package application;

import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import titan.ccp.model.records.ActivePowerRecord;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class MapTimeFormat extends SimpleFunction<KV<String, ActivePowerRecord>, KV<HourOfDayKey,
    ActivePowerRecord>> {
    private final ZoneId zone = ZoneId.of("Europe/Paris");
  final StatsKeyFactory<HourOfDayKey> keyFactory = new HourOfDayKeyFactory();

    @Override
    public KV<application.HourOfDayKey, ActivePowerRecord> apply(
    final KV<String, ActivePowerRecord> kv) {
      final Instant instant = Instant.ofEpochMilli(kv.getValue().getTimestamp());
      final LocalDateTime dateTime = LocalDateTime.ofInstant(instant, this.zone);
      return KV.of(keyFactory.createKey(kv.getValue().getIdentifier(), dateTime),
          kv.getValue());
    }
  }
}
