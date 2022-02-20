package theodolite.commons.workloadgeneration;

import java.io.IOException;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * A factory for creating {@link PubSubRecordSender}s that sends Titan {@link ActivePowerRecord}s.
 */
public final class TitanPubSubSenderFactory {

  private TitanPubSubSenderFactory() {}

  /**
   * Create a new {@link PubSubRecordSender} for {@link ActivePowerRecord}s for the given PubSub
   * configuration.
   */
  public static PubSubRecordSender<ActivePowerRecord> forPubSubConfig(final String topic) {
    return PubSubRecordSender
        .<ActivePowerRecord>builder(topic, r -> {
          try {
            return r.toByteBuffer();
          } catch (final IOException e) {
            throw new IllegalStateException(e);
          }
        })
        // .orderingKeyAccessor(r -> r.getIdentifier())
        .timestampAccessor(r -> r.getTimestamp())
        .build();
  }
}
