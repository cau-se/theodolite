package theodolite.uc4.application;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.DoubleSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import theodolite.commons.hazelcastjet.ConfigurationKeys;
import theodolite.uc4.application.uc4specifics.EventDeserializer;
import titan.ccp.configuration.events.EventSerde;

/**
 * Builds a read and write Properties objects containing the needed kafka properties used for the
 * UC4 benchmark of Hazelcast Jet.
 */
public class Uc4KafkaPropertiesBuilder {

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
    props.put("bootstrap.servers", kafkaBootstrapServers); //NOCS
    props.put("key.deserializer", StringDeserializer.class.getCanonicalName());
    props.put("value.deserializer", KafkaAvroDeserializer.class);
    props.put("specific.avro.reader", true);
    props.put("schema.registry.url", schemaRegistryUrl);
    props.put("auto.offset.reset", "latest");
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
    props.put("bootstrap.servers", kafkaBootstrapServers); //NOCS
    props.put("key.deserializer", StringDeserializer.class.getCanonicalName());
    props.put("value.deserializer", DoubleDeserializer.class.getCanonicalName());
    props.put("specific.avro.reader", true);
    props.put("schema.registry.url", schemaRegistryUrl);
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
    props.put("bootstrap.servers", kafkaBootstrapServers); //NOCS
    props.put("key.deserializer", EventDeserializer.class);
    props.put("value.deserializer", StringDeserializer.class.getCanonicalName());
    props.put("specific.avro.reader", true);
    props.put("schema.registry.url", schemaRegistryUrl);
    props.put("auto.offset.reset", "earliest");
    return props;
  }

  /**
   * Builds Kafka Properties used for the UC4 Benchmark pipeline.
   * 
   * @param kafkaBootstrapServerDefault Default bootstrap server if not set by environment.
   * @return A Kafka Properties Object containing the values needed for a Hazelcast Jet UC4
   *         Pipeline.
   */
  public Properties buildKafkaWritePropsFromEnv(final String kafkaBootstrapServerDefault) {

    final String kafkaBootstrapServers = Objects.requireNonNullElse(
        System.getenv(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS),
        kafkaBootstrapServerDefault);

    final Properties props = new Properties();
    props.put("bootstrap.servers", kafkaBootstrapServers); //NOCS
    props.put("key.serializer", StringSerializer.class.getCanonicalName());
    props.put("value.serializer", DoubleSerializer.class.getCanonicalName());
    return props;
  }

}
