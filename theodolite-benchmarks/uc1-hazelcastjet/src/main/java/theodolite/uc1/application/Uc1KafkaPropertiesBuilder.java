package theodolite.uc1.application;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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

    // comment:
    // > Could not find constant fields for all properties
    // > setProperties not applicable for non string values
    final Properties props = new Properties();
    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
    props.setProperty("schema.registry.url", schemaRegistryUrl);
    props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class.getCanonicalName());
    props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        KafkaAvroDeserializer.class.getCanonicalName());
    props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put("specific.avro.reader", true);


    return props;
  }

}
