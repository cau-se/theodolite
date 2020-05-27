package theodolite.uc4.streamprocessing;

import java.time.Duration;
import java.util.Objects;
import org.apache.kafka.streams.Topology;
import theodolite.commons.kafkastreams.KafkaStreamsBuilder;

/**
 * Builder for the Kafka Streams configuration.
 */
public class Uc4KafkaStreamsBuilder extends KafkaStreamsBuilder {

  private String inputTopic; // NOPMD
  private String outputTopic; // NOPMD
  private Duration aggregtionDuration; // NOPMD
  private Duration aggregationAdvance; // NOPMD

  public Uc4KafkaStreamsBuilder inputTopic(final String inputTopic) {
    this.inputTopic = inputTopic;
    return this;
  }

  public Uc4KafkaStreamsBuilder outputTopic(final String outputTopic) {
    this.outputTopic = outputTopic;
    return this;
  }

  public Uc4KafkaStreamsBuilder aggregtionDuration(final Duration aggregtionDuration) {
    this.aggregtionDuration = aggregtionDuration;
    return this;
  }

  public Uc4KafkaStreamsBuilder aggregationAdvance(final Duration aggregationAdvance) {
    this.aggregationAdvance = aggregationAdvance;
    return this;
  }

  @Override
  protected Topology buildTopology() {
    Objects.requireNonNull(this.inputTopic, "Input topic has not been set.");
    Objects.requireNonNull(this.outputTopic, "Output topic has not been set.");
    Objects.requireNonNull(this.aggregtionDuration, "Aggregation duration has not been set.");
    Objects.requireNonNull(this.aggregationAdvance, "Aggregation advance period has not been set.");

    final TopologyBuilder topologyBuilder = new TopologyBuilder(
        this.inputTopic,
        this.outputTopic,
        this.aggregtionDuration,
        this.aggregationAdvance);

    return topologyBuilder.build();
  }

}
