package theodolite.uc4.application;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import theodolite.commons.hazelcastjet.ConfigurationKeys;
import theodolite.uc4.application.uc4specifics.EventDeserializer;

/**
 * Builds a read and write Properties objects containing the needed kafka properties used for the
 * UC4 benchmark of Hazelcast Jet.
 */
public class Uc4KafkaPropertiesBuilder {

  private static final String SPECIFIC_AVRO_READER_CONFIG = "specific.avro.reader";
  private static final String SCHEMA_REGISTRY_URL_CONFIG = "schema.registry.url";
  private static final String TRUE = "true";

  /**
   * Builds Kafka Properties used for the UC4 Benchmark pipeline.
   *
   * @param kafkaBootstrapServerDefault Default bootstrap server if not set by envrionment.
   * @param schemaRegistryUrlDefault Default schema registry URL if not set by environment.
   * @return A Kafka Properties Object containing the values needed for a Hazelcast Jet UC4
   *         Pipeline.
   */
  public Properties buildKafkaInputReadPropsFromEnv(final String kafkaBootstrapServerDefault,
      final String schemaRegistryUrlDefault) {

    final String kafkaBootstrapServers = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS),
        kafkaBootstrapServerDefault);
    final String schemaRegistryUrl = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.SCHEMA_REGISTRY_URL),
        schemaRegistryUrlDefault);

    final Properties props = new Properties();
    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers); // NOCS
    props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getCanonicalName());
    props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        KafkaAvroDeserializer.class.getCanonicalName());
    props.setProperty(SPECIFIC_AVRO_READER_CONFIG, TRUE);
    props.setProperty(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    return props;
  }

  /**
   * Builds Kafka Properties used for the UC4 Benchmark pipeline.
   *
   * @param kafkaBootstrapServerDefault Default bootstrap server if not set by envrionment.
   * @param schemaRegistryUrlDefault Default schema registry URL if not set by environment.
   * @return A Kafka Properties Object containing the values needed for a Hazelcast Jet UC4
   *         Pipeline.
   */
  public Properties buildKafkaAggregationReadPropsFromEnv(final String kafkaBootstrapServerDefault,
      final String schemaRegistryUrlDefault) {

    final String kafkaBootstrapServers = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS),
        kafkaBootstrapServerDefault);
    final String schemaRegistryUrl = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.SCHEMA_REGISTRY_URL),
        schemaRegistryUrlDefault);

    final Properties props = new Properties();
    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers); // NOCS
    props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getCanonicalName());
    props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        KafkaAvroDeserializer.class.getCanonicalName());
    props.setProperty(SPECIFIC_AVRO_READER_CONFIG, TRUE);
    props.setProperty(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

    return props;
  }

  /**
   * Builds Kafka Properties used for the UC4 Benchmark pipeline.
   *
   * @param kafkaBootstrapServerDefault Default bootstrap server if not set by envrionment.
   * @param schemaRegistryUrlDefault Default schema registry URL if not set by environment.
   * @return A Kafka Properties Object containing the values needed for a Hazelcast Jet UC4
   *         Pipeline.
   */
  public Properties buildKafkaConfigReadPropsFromEnv(final String kafkaBootstrapServerDefault,
      final String schemaRegistryUrlDefault) {

    final String kafkaBootstrapServers = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS),
        kafkaBootstrapServerDefault);
    final String schemaRegistryUrl = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.SCHEMA_REGISTRY_URL),
        schemaRegistryUrlDefault);

    final Properties props = new Properties();
    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
    props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        EventDeserializer.class.getCanonicalName());
    props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getCanonicalName());
    props.setProperty(SPECIFIC_AVRO_READER_CONFIG, TRUE);
    props.setProperty(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    return props;
  }

  /**
   * Builds Kafka Properties used for the UC4 Benchmark pipeline.
   *
   * @param kafkaBootstrapServerDefault Default bootstrap server if not set by environment.
   * @return A Kafka Properties Object containing the values needed for a Hazelcast Jet UC4
   *         Pipeline.
   */
  public Properties buildKafkaWritePropsFromEnv(final String kafkaBootstrapServerDefault,
                                                final String schemaRegistryUrlDefault) {

    final String kafkaBootstrapServers = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS),
        kafkaBootstrapServerDefault);
    final String schemaRegistryUrl = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.SCHEMA_REGISTRY_URL),
        schemaRegistryUrlDefault);

    final Properties props = new Properties();
    props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers); // NOCS
    props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        StringSerializer.class.getCanonicalName());
    props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        KafkaAvroSerializer.class.getCanonicalName());
    props.setProperty("specific.avro.writer", TRUE);
    props.setProperty(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

    return props;
  }

}
