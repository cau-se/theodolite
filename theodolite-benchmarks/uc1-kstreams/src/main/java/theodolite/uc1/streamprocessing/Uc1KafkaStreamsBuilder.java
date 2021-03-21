package theodolite.uc1.streamprocessing;

import java.util.Objects;
import java.util.Properties;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.streams.Topology;
import theodolite.commons.kafkastreams.KafkaStreamsBuilder;
import titan.ccp.common.kafka.avro.SchemaRegistryAvroSerdeFactory;

/**
 * Builder for the Kafka Streams configuration.
 */
public class Uc1KafkaStreamsBuilder extends KafkaStreamsBuilder {

  public Uc1KafkaStreamsBuilder(final Configuration config) {
    super(config);
  }

  @Override
  protected Topology buildTopology(final Properties properties) {
    Objects.requireNonNull(this.inputTopic, "Input topic has not been set.");
    return new TopologyBuilder(this.inputTopic,
        new SchemaRegistryAvroSerdeFactory(this.schemaRegistryUrl)).build(properties);
  }
}
