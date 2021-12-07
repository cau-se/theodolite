package theodolite.uc1.application;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.common.serialization.StringDeserializer;
import theodolite.commons.hazelcastjet.ConfigurationKeys;

/**
 * Builds a Properties object containing the needed kafka properties used for the UC1 benchmark of
 * Hazelcast Jet.
 */
public class Uc1KafkaPropertiesBuilder {

  /**
   * Builds Kafka Properties used for the UC1 Benchmark pipeline.
   *
   * @param kafkaBootstrapServerDefault Default bootstrap server if not net by envrionment.
   * @param schemaRegistryUrlDefault Default schema registry URL if not set by environment.
   * @return A Kafka Properties Object containing the values needed for a Hazelcast Jet UC1
   *         Pipeline.
   */
  public Properties buildKafkaPropsFromEnv(final String kafkaBootstrapServerDefault,
      final String schemaRegistryUrlDefault) {

    final String kafkaBootstrapServers = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS),
        kafkaBootstrapServerDefault);
    final String schemaRegistryUrl = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.SCHEMA_REGISTRY_URL),
        schemaRegistryUrlDefault);

    final Properties props = new Properties();
    props.put("bootstrap.servers", kafkaBootstrapServers);
    props.put("key.deserializer", StringDeserializer.class.getCanonicalName());
    props.put("value.deserializer", KafkaAvroDeserializer.class);
    props.put("specific.avro.reader", true);
    props.put("schema.registry.url", schemaRegistryUrl);
    props.setProperty("auto.offset.reset", "earliest");
    return props;
  }

}
