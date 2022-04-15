package rocks.theodolite.benchmarks.loadgenerator;

import java.io.IOException;
import java.nio.ByteBuffer;
import rocks.theodolite.commons.model.records.ActivePowerRecord;

/**
 * A factory for creating {@link PubSubRecordSender}s that sends Titan {@link ActivePowerRecord}s.
 */
public final class TitanPubSubSenderFactory {

  private TitanPubSubSenderFactory() {}

  /**
   * Create a new {@link PubSubRecordSender} for {@link ActivePowerRecord}s for the given Pub/Sub
   * configuration.
   */
  public static PubSubRecordSender<ActivePowerRecord> forPubSubConfig(
      final String project,
      final String topic) {
    return PubSubRecordSender
        .builderForProject(project, topic, TitanPubSubSenderFactory::serialize)
        // .orderingKeyAccessor(r -> r.getIdentifier())
        .timestampAccessor(r -> r.getTimestamp())
        .build();
  }

  /**
   * Create a new {@link PubSubRecordSender} for {@link ActivePowerRecord}s for the given PubSub
   * emulator configuration.
   */
  public static PubSubRecordSender<ActivePowerRecord> forEmulatedPubSubConfig(
      final String emulatorHost,
      final String topic) {
    return PubSubRecordSender
        .builderForEmulator(emulatorHost, topic, TitanPubSubSenderFactory::serialize)
        // .orderingKeyAccessor(r -> r.getIdentifier())
        .timestampAccessor(r -> r.getTimestamp())
        .build();
  }

  private static ByteBuffer serialize(final ActivePowerRecord record) {
    try {
      return record.toByteBuffer();
    } catch (final IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
