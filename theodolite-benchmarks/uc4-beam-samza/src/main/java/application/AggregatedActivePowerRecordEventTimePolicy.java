package application;

import java.util.Optional;

import org.apache.beam.sdk.io.kafka.KafkaRecord;
import org.apache.beam.sdk.io.kafka.TimestampPolicy;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.joda.time.Instant;
import titan.ccp.model.records.AggregatedActivePowerRecord;

/**
 * TimeStampPolicy to use event time based on the timestamp of the record value.
 */
public class AggregatedActivePowerRecordEventTimePolicy
    extends TimestampPolicy<String, AggregatedActivePowerRecord> {
  protected Instant currentWatermark;

  public AggregatedActivePowerRecordEventTimePolicy(final Optional<Instant> previousWatermark) {
    this.currentWatermark = previousWatermark.orElse(BoundedWindow.TIMESTAMP_MIN_VALUE);
  }


  @Override
  public Instant getTimestampForRecord(final PartitionContext ctx,
      final KafkaRecord<String, AggregatedActivePowerRecord> record) {
    this.currentWatermark = new Instant(record.getKV().getValue().getTimestamp());
    return this.currentWatermark;
  }

  @Override
  public Instant getWatermark(final PartitionContext ctx) {
    return this.currentWatermark;
  }

}
