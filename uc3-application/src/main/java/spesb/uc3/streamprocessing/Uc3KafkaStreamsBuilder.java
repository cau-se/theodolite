package spesb.uc3.streamprocessing;

import java.time.Duration;
import java.util.Objects;
import org.apache.kafka.streams.Topology;
import spesb.commons.kafkastreams.KafkaStreamsBuilder;

/**
 * Builder for the Kafka Streams configuration.
 */
public class Uc3KafkaStreamsBuilder extends KafkaStreamsBuilder {

  private String inputTopic; // NOPMD
  private String outputTopic; // NOPMD
  private Duration windowDuration; // NOPMD

  public Uc3KafkaStreamsBuilder inputTopic(final String inputTopic) {
    this.inputTopic = inputTopic;
    return this;
  }

  public Uc3KafkaStreamsBuilder outputTopic(final String outputTopic) {
    this.outputTopic = outputTopic;
    return this;
  }

  public Uc3KafkaStreamsBuilder windowDuration(final Duration windowDuration) {
    this.windowDuration = windowDuration;
    return this;
  }

  @Override
  protected Topology buildTopology() {
    Objects.requireNonNull(this.inputTopic, "Input topic has not been set.");
    Objects.requireNonNull(this.outputTopic, "Output topic has not been set.");
    Objects.requireNonNull(this.windowDuration, "Window duration has not been set.");

    final TopologyBuilder topologyBuilder = new TopologyBuilder(this.inputTopic, this.outputTopic,
        this.windowDuration);
    return topologyBuilder.build();
  }

}
