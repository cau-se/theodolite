package theodolite.uc3.streamprocessing;

import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.streams.Topology;
import theodolite.commons.kafkastreams.KafkaStreamsBuilder;
import titan.ccp.common.kafka.avro.SchemaRegistryAvroSerdeFactory;

/**
 * Builder for the Kafka Streams configuration.
 */
public class Uc3KafkaStreamsBuilder extends KafkaStreamsBuilder {

  private String outputTopic; // NOPMD
  private Duration aggregtionDuration; // NOPMD
  private Duration aggregationAdvance; // NOPMD

  public Uc3KafkaStreamsBuilder(final Configuration config) {
    super(config);
  }

  public Uc3KafkaStreamsBuilder outputTopic(final String outputTopic) {
    this.outputTopic = outputTopic;
    return this;
  }

  public Uc3KafkaStreamsBuilder aggregtionDuration(final Duration aggregtionDuration) {
    this.aggregtionDuration = aggregtionDuration;
    return this;
  }

  public Uc3KafkaStreamsBuilder aggregationAdvance(final Duration aggregationAdvance) {
    this.aggregationAdvance = aggregationAdvance;
    return this;
  }

  @Override
  protected Topology buildTopology(final Properties properties) {
    Objects.requireNonNull(this.inputTopic, "Input topic has not been set.");
    Objects.requireNonNull(this.outputTopic, "Output topic has not been set.");
    Objects.requireNonNull(this.aggregtionDuration, "Aggregation duration has not been set.");
    Objects.requireNonNull(this.aggregationAdvance, "Aggregation advance period has not been set.");

    final TopologyBuilder topologyBuilder = new TopologyBuilder(
        this.inputTopic,
        this.outputTopic,
        new SchemaRegistryAvroSerdeFactory(this.schemaRegistryUrl),
        this.aggregtionDuration,
        this.aggregationAdvance);

    return topologyBuilder.build(properties);
  }

}
