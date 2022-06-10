package rocks.theodolite.benchmarks.uc2.kstreams;

import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.streams.Topology;
import rocks.theodolite.benchmarks.commons.kafka.avro.SchemaRegistryAvroSerdeFactory;
import rocks.theodolite.benchmarks.commons.kstreams.KafkaStreamsBuilder;

/**
 * Builder for the Kafka Streams configuration.
 */
public class Uc2KafkaStreamsBuilder extends KafkaStreamsBuilder {

  private String outputTopic; // NOPMD
  private Duration windowDuration; // NOPMD

  public Uc2KafkaStreamsBuilder(final Configuration config) {
    super(config);
  }

  public Uc2KafkaStreamsBuilder outputTopic(final String outputTopic) {
    this.outputTopic = outputTopic;
    return this;
  }

  public Uc2KafkaStreamsBuilder windowDuration(final Duration windowDuration) {
    this.windowDuration = windowDuration;
    return this;
  }

  @Override
  protected Topology buildTopology(final Properties properties) {
    Objects.requireNonNull(this.inputTopic, "Input topic has not been set.");
    Objects.requireNonNull(this.outputTopic, "Output topic has not been set.");
    Objects.requireNonNull(this.windowDuration, "Window duration has not been set.");

    final TopologyBuilder topologyBuilder = new TopologyBuilder(this.inputTopic, this.outputTopic,
        new SchemaRegistryAvroSerdeFactory(this.schemaRegistryUrl), this.windowDuration);
    return topologyBuilder.build(properties);
  }

}
