package theodolite.commons.workloadgeneration.functions;

import java.util.Properties;
import theodolite.commons.workloadgeneration.communication.kafka.KafkaRecordSender;
import titan.ccp.model.records.ActivePowerRecord;

public final class TitanMessageGeneratorFactory {

  private final RecordSender<ActivePowerRecord> recordSender;

  private TitanMessageGeneratorFactory(final RecordSender<ActivePowerRecord> recordSender) {
    this.recordSender = recordSender;
  }

  public MessageGenerator forConstantValue(final double value) {
    return MessageGenerator.from(
        sensor -> new ActivePowerRecord(sensor, System.currentTimeMillis(), value),
        this.recordSender);
  }

  public static TitanMessageGeneratorFactory withKafkaConfig(
      final String bootstrapServers,
      final String topic,
      final String schemaRegistryUrl) {
    return withKafkaConfig(bootstrapServers, topic, schemaRegistryUrl, new Properties());
  }

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
