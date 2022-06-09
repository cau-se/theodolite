package rocks.theodolite.benchmarks.loadgenerator;

import java.util.Properties;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;

/**
 * A factory for creating {@link KafkaRecordSender}s that sends Titan {@link ActivePowerRecord}s.
 */
public final class TitanKafkaSenderFactory {

  private TitanKafkaSenderFactory() {}

  /**
   * Create a new KafkaRecordSender for {@link ActivePowerRecord}s for the given Kafka
   * configuration.
   */
  public static KafkaRecordSender<ActivePowerRecord> forKafkaConfig(
      final String bootstrapServers,
      final String topic,
      final String schemaRegistryUrl) {
    return forKafkaConfig(bootstrapServers, topic, schemaRegistryUrl, new Properties());
  }

  /**
   * Create a new KafkaRecordSender for {@link ActivePowerRecord}s for the given Kafka
   * configuration.
   */
  public static KafkaRecordSender<ActivePowerRecord> forKafkaConfig(
      final String bootstrapServers,
      final String topic,
      final String schemaRegistryUrl,
      final Properties properties) {
    return KafkaRecordSender
        .<ActivePowerRecord>builderWithSchemaRegistry(
            bootstrapServers,
            topic,
            schemaRegistryUrl)
        .keyAccessor(r -> r.getIdentifier())
        .timestampAccessor(r -> r.getTimestamp())
        .build();
  }
}
