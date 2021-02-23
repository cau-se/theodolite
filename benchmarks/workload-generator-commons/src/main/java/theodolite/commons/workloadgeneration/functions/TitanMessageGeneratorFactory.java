package theodolite.commons.workloadgeneration.functions;

import java.util.Properties;
import theodolite.commons.workloadgeneration.communication.kafka.KafkaRecordSender;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * A factory for creating {@link MessageGenerator}s that creates Titan {@link ActivePowerRecord}s
 * and sends them via Kafka.
 */
public final class TitanMessageGeneratorFactory {

  private final RecordSender<ActivePowerRecord> recordSender;

  private TitanMessageGeneratorFactory(final RecordSender<ActivePowerRecord> recordSender) {
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
   * Create a new TitanMessageGeneratorFactory for the given Kafka configuration.
   */
  public static TitanMessageGeneratorFactory withKafkaConfig(
      final String bootstrapServers,
      final String topic,
      final String schemaRegistryUrl) {
    return withKafkaConfig(bootstrapServers, topic, schemaRegistryUrl, new Properties());
  }

  /**
   * Create a new TitanMessageGeneratorFactory for the given Kafka configuration.
   */
  public static TitanMessageGeneratorFactory withKafkaConfig(
      final String bootstrapServers,
      final String topic,
      final String schemaRegistryUrl,
      final Properties properties) {
    final KafkaRecordSender<ActivePowerRecord> kafkaRecordSender = KafkaRecordSender
        .<ActivePowerRecord>builder(
            bootstrapServers,
            topic,
            schemaRegistryUrl)
        .keyAccessor(r -> r.getIdentifier())
        .timestampAccessor(r -> r.getTimestamp())
        .build();
    return new TitanMessageGeneratorFactory(kafkaRecordSender);
  }

}
