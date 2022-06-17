package rocks.theodolite.benchmarks.commons.hazelcastjet;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;


/**
 * Generalized builder for Kafka properties.
 * Will always set AUTO_OFFSET_RESET_CONFIG to earliest
 */
public class KafkaPropertiesBuilder {

  private static final String TRUE = "true";
  private static final String AUTO_OFFSET_RESET_CONFIG = "earliest";

  private static final String SPECIFIC_AVRO_WRITER = "specific.avro.writer";

  private final Properties readProperties;

  private final Properties writeProperties;

  /**
   * Constructs a new PropertiesBuilder with defined default read and write properties.
   * @param kafkaBootstrapServer default boostrap address property.
   * @param schemaRegistryUrl default schema registry address property.
   * @param jobName default job name property.
   */
  public KafkaPropertiesBuilder(final String kafkaBootstrapServer,
                                final String schemaRegistryUrl,
                                final String jobName) {

    this.writeProperties = new Properties();
    this.readProperties = new Properties();
    readProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer);
    readProperties.setProperty(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
        schemaRegistryUrl);

    writeProperties.putAll(readProperties);

    readProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, jobName);
    readProperties.setProperty(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, TRUE);
    readProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET_CONFIG);
    readProperties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, TRUE);

    writeProperties.setProperty(SPECIFIC_AVRO_WRITER, TRUE);
  }

  /**
   * Returns default read properties with the defined deserializers.
   * @param keyDeserializer deserializer for the key.
   * @param valueDeserializer deserializer for the value.
   */
  public Properties buildReadProperties(final String keyDeserializer,
                                        final String valueDeserializer) {

    final Properties props = new Properties();
    props.putAll(this.readProperties);
    props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        keyDeserializer);
    props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        valueDeserializer);
    return props;
  }

  /**
   * Returns default read properties with the defined Serializers.
   * @param keySerializer serializer for the key.
   * @param valueSerializer serializer for the value.
   */
  public Properties buildWriteProperties(final String keySerializer,
                                        final String valueSerializer) {

    final Properties props = new Properties();
    props.putAll(this.writeProperties);
    props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        keySerializer);
    props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        valueSerializer);
    return props;
  }


  /**
   * Builds Kafka Properties used for the UC4 Benchmark pipeline.
   *
   * @param kafkaBootstrapServerDefault Default bootstrap server if not set by environment.
   * @param schemaRegistryUrlDefault Default schema registry URL if not set by environment.
   * @param applicationName Used to set the group id to commit the offsets
   * @param keyDeserializer Classname for the key deserializer.
   * @param valueDeserializer Classname for the value deserializer.
   * @return A Kafka Properties Object containing the values needed for a Pipeline.
   */
  public Properties buildKafkaInputReadPropsFromEnv(final String kafkaBootstrapServerDefault,//NOPMD
                                                    final String schemaRegistryUrlDefault,
                                                    final String applicationName,
                                                    final String keyDeserializer,
                                                    final String valueDeserializer) {

    final String kafkaBootstrapServers = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS),
        kafkaBootstrapServerDefault);
    final String schemaRegistryUrl = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.SCHEMA_REGISTRY_URL),
        schemaRegistryUrlDefault);

    final Properties props = new Properties();
    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
    props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        keyDeserializer);
    props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        valueDeserializer);
    props.setProperty(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    props.setProperty(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, TRUE);
    props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET_CONFIG);

    props.setProperty(ConsumerConfig.GROUP_ID_CONFIG,applicationName);
    props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,TRUE);

    return props;
  }

  /**
   * Builds Kafka Properties used for the UC4 Benchmark pipeline.
   *
   * @param kafkaBootstrapServerDefault Default bootstrap server if not set by environment.
   * @param schemaRegistryUrlDefault Default schema registry URL if not set by environment.
   * @param keySerializer Classname for the key serializer.
   * @param valueSerializer Classname for the value serializer.
   * @return A Kafka Properties Object containing the values needed for a Hazelcast Jet UC4
   *         Pipeline.
   */
  public Properties buildKafkaWritePropsFromEnv(final String kafkaBootstrapServerDefault,//NOPMD
                                                final String schemaRegistryUrlDefault,
                                                final String keySerializer,
                                                final String valueSerializer) {

    final String kafkaBootstrapServers = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS),
        kafkaBootstrapServerDefault);
    final String schemaRegistryUrl = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.SCHEMA_REGISTRY_URL),
        schemaRegistryUrlDefault);

    final Properties props = new Properties();
    props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
    props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        keySerializer);
    props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        valueSerializer);
    props.setProperty(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    props.setProperty(SPECIFIC_AVRO_WRITER, TRUE);

    return props;
  }

}
