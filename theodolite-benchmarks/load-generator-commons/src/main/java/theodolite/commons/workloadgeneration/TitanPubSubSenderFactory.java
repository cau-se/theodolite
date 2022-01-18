package theodolite.commons.workloadgeneration;

import java.util.Properties;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * A factory for creating {@link KafkaRecordSender}s that sends Titan {@link ActivePowerRecord}s.
 */
public final class TitanPubSubSenderFactory {

  private TitanPubSubSenderFactory() {}

  /**
   * Create a new KafkaRecordSender for {@link ActivePowerRecord}s for the given Kafka
   * configuration.
   */
  public static PubSubRecordSender<ActivePowerRecord> forKafkaConfig(
      final String bootstrapServers,
      final String topic,
      final String schemaRegistryUrl) {
    return forKafkaConfig(bootstrapServers, topic, schemaRegistryUrl, new Properties());
  }

  /**
   * Create a new KafkaRecordSender for {@link ActivePowerRecord}s for the given Kafka
   * configuration.
   */
  public static PubSubRecordSender<ActivePowerRecord> forKafkaConfig(
      final String bootstrapServers,
      final String topic,
      final String schemaRegistryUrl,
      final Properties properties) {
    return PubSubRecordSender
        .<ActivePowerRecord>builder(
            bootstrapServers,
            topic,
            schemaRegistryUrl)
        .keyAccessor(r -> r.getIdentifier())
        .timestampAccessor(r -> r.getTimestamp())
        .build();
  }
}
