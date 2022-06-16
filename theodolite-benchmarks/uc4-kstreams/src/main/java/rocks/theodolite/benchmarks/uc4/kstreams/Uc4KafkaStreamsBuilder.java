package rocks.theodolite.benchmarks.uc4.kstreams;

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
public class Uc4KafkaStreamsBuilder extends KafkaStreamsBuilder { // NOPMD builder method

  private static final Duration EMIT_PERIOD_DEFAULT = Duration.ofSeconds(1);
  private static final Duration GRACE_PERIOD_DEFAULT = Duration.ZERO;

  private String feedbackTopic; // NOPMD
  private String outputTopic; // NOPMD
  private String configurationTopic; // NOPMD
  private Duration emitPeriod; // NOPMD
  private Duration gracePeriod; // NOPMD

  public Uc4KafkaStreamsBuilder(final Configuration config) {
    super(config);
  }

  public Uc4KafkaStreamsBuilder feedbackTopic(final String feedbackTopic) {
    this.feedbackTopic = feedbackTopic;
    return this;
  }

  public Uc4KafkaStreamsBuilder outputTopic(final String outputTopic) {
    this.outputTopic = outputTopic;
    return this;
  }

  public Uc4KafkaStreamsBuilder configurationTopic(final String configurationTopic) {
    this.configurationTopic = configurationTopic;
    return this;
  }

  public Uc4KafkaStreamsBuilder emitPeriod(final Duration emitPeriod) {
    this.emitPeriod = Objects.requireNonNull(emitPeriod);
    return this;
  }

  public Uc4KafkaStreamsBuilder gracePeriod(final Duration gracePeriod) {
    this.gracePeriod = Objects.requireNonNull(gracePeriod);
    return this;
  }

  @Override
  protected Topology buildTopology(final Properties properties) {
    Objects.requireNonNull(this.inputTopic, "Input topic has not been set.");
    Objects.requireNonNull(this.feedbackTopic, "Feedback topic has not been set.");
    Objects.requireNonNull(this.outputTopic, "Output topic has not been set.");
    Objects.requireNonNull(this.configurationTopic, "Configuration topic has not been set.");

    final TopologyBuilder topologyBuilder = new TopologyBuilder(
        this.inputTopic,
        this.outputTopic,
        this.feedbackTopic,
        this.configurationTopic,
        this.emitPeriod == null ? EMIT_PERIOD_DEFAULT : this.emitPeriod,
        this.gracePeriod == null ? GRACE_PERIOD_DEFAULT : this.gracePeriod,
        new SchemaRegistryAvroSerdeFactory(this.schemaRegistryUrl));

    return topologyBuilder.build(properties);
  }

}
