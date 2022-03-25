package rocks.theodolite.benchmarks.commons.hazelcastjet;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;


public class KafkaPropertiesBuilder {

  private static final String TRUE = "true";
  private static final String AUTO_OFFSET_RESET_CONFIG = "earliest";


  /**
   * Builds Kafka Properties used for the UC4 Benchmark pipeline.
   *
   * @param kafkaBootstrapServerDefault Default bootstrap server if not set by environment.
   * @param schemaRegistryUrlDefault Default schema registry URL if not set by environment.
   * @return A Kafka Properties Object containing the values needed for a Pipeline.
   */
  public Properties buildKafkaInputReadPropsFromEnv(final String kafkaBootstrapServerDefault,
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
   * @return A Kafka Properties Object containing the values needed for a Hazelcast Jet UC4
   *         Pipeline.
   */
  public Properties buildKafkaWritePropsFromEnv(final String kafkaBootstrapServerDefault,
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
    props.setProperty("specific.avro.writer", TRUE);

    return props;
  }





}
