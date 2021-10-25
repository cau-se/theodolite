package application;

import java.util.Optional;

import org.apache.beam.sdk.io.kafka.KafkaRecord;
import org.apache.beam.sdk.io.kafka.TimestampPolicy;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.joda.time.Instant;

/**
 * TimeStampPolicy to use event time based on the timestamp of the record value.
 */
public class EventTimePolicy
    extends TimestampPolicy<String, titan.ccp.model.records.ActivePowerRecord> {
  protected Instant currentWatermark;

  public EventTimePolicy(final Optional<Instant> previousWatermark) {
    super();
    this.currentWatermark = previousWatermark.orElse(BoundedWindow.TIMESTAMP_MIN_VALUE);
  }


  @Override
  public Instant getTimestampForRecord(final PartitionContext ctx,
      final KafkaRecord<String, titan.ccp.model.records.ActivePowerRecord> record) {
    this.currentWatermark = new Instant(record.getKV().getValue().getTimestamp());
    return this.currentWatermark;
  }

  @Override
  public Instant getWatermark(final PartitionContext ctx) {
    return this.currentWatermark;
  }

}
