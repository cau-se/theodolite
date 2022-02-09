package theodolite.uc2.application;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import theodolite.commons.hazelcastjet.ConfigurationKeys;

/**
 * Builds a read and write Properties objects containing the needed kafka properties used for the
 * UC2 benchmark of Hazelcast Jet.
 */
public class Uc2KafkaPropertiesBuilder {

  /**
   * Builds Kafka Properties used for the UC2 Benchmark pipeline.
   *
   * @param kafkaBootstrapServerDefault Default bootstrap server if not set by envrionment.
   * @param schemaRegistryUrlDefault Default schema registry URL if not set by environment.
   * @return A Kafka Properties Object containing the values needed for a Hazelcast Jet UC2
   *         Pipeline.
   */
  public Properties buildKafkaReadPropsFromEnv(final String kafkaBootstrapServerDefault,
      final String schemaRegistryUrlDefault) {

    final String kafkaBootstrapServers = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS),
        kafkaBootstrapServerDefault);
    final String schemaRegistryUrl = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.SCHEMA_REGISTRY_URL),
        schemaRegistryUrlDefault);

    // comment:
    // > Could not find constant fields for all properties
    // > setProperties not applicable for non string values
    final Properties props = new Properties();
    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers); // NOCS
    props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getCanonicalName());
    props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        KafkaAvroDeserializer.class.getCanonicalName());
    props.put("specific.avro.reader", true);
    props.setProperty("schema.registry.url", schemaRegistryUrl);
    props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    return props;
  }

  /**
   * Builds Kafka Properties used for the UC2 Benchmark pipeline.
   *
   * @param kafkaBootstrapServerDefault Default bootstrap server if not set by environment.
   * @return A Kafka Properties Object containing the values needed for a Hazelcast Jet UC2
   *         Pipeline.
   */
  public Properties buildKafkaWritePropsFromEnv(final String kafkaBootstrapServerDefault) {

    final String kafkaBootstrapServers = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS),
        kafkaBootstrapServerDefault);

    final Properties props = new Properties();
    props.put("bootstrap.servers", kafkaBootstrapServers); // NOCS
    props.put("key.serializer", StringSerializer.class.getCanonicalName());
    props.put("value.serializer", StringSerializer.class.getCanonicalName());
    return props;
  }

}
