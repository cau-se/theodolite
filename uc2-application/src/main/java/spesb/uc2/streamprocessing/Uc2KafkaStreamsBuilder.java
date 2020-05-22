package spesb.uc2.streamprocessing;

import java.time.Duration;
import java.util.Objects;
import org.apache.kafka.streams.Topology;
import spesb.commons.kafkastreams.KafkaStreamsBuilder;

/**
 * Builder for the Kafka Streams configuration.
 */
public class Uc2KafkaStreamsBuilder extends KafkaStreamsBuilder { // NOPMD builder method

  private static final Duration WINDOW_SIZE_DEFAULT = Duration.ofSeconds(1);
  private static final Duration GRACE_PERIOD_DEFAULT = Duration.ZERO;

  private String inputTopic; // NOPMD
  private String outputTopic; // NOPMD
  private String configurationTopic; // NOPMD
  private Duration windowSize; // NOPMD
  private Duration gracePeriod; // NOPMD

  public Uc2KafkaStreamsBuilder inputTopic(final String inputTopic) {
    this.inputTopic = inputTopic;
    return this;
  }

  public Uc2KafkaStreamsBuilder outputTopic(final String outputTopic) {
    this.outputTopic = outputTopic;
    return this;
  }

  public Uc2KafkaStreamsBuilder configurationTopic(final String configurationTopic) {
    this.configurationTopic = configurationTopic;
    return this;
  }

  public Uc2KafkaStreamsBuilder windowSize(final Duration windowSize) {
    this.windowSize = Objects.requireNonNull(windowSize);
    return this;
  }

  public Uc2KafkaStreamsBuilder gracePeriod(final Duration gracePeriod) {
    this.gracePeriod = Objects.requireNonNull(gracePeriod);
    return this;
  }

  @Override
  protected Topology buildTopology() {
    Objects.requireNonNull(this.inputTopic, "Input topic has not been set.");
    Objects.requireNonNull(this.outputTopic, "Output topic has not been set.");
    Objects.requireNonNull(this.configurationTopic, "Configuration topic has not been set.");

    final TopologyBuilder topologyBuilder = new TopologyBuilder(
        this.inputTopic,
        this.outputTopic,
        this.configurationTopic,
        this.windowSize == null ? WINDOW_SIZE_DEFAULT : this.windowSize,
        this.gracePeriod == null ? GRACE_PERIOD_DEFAULT : this.gracePeriod);

    return topologyBuilder.build();
  }

}
