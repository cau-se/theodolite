package spesb.uc1.streamprocessing;

import java.util.Objects;
import org.apache.kafka.streams.Topology;
import spesb.commons.kafkastreams.KafkaStreamsBuilder;

/**
 * Builder for the Kafka Streams configuration.
 */
public class Uc1KafkaStreamsBuilder extends KafkaStreamsBuilder {
  private String inputTopic; // NOPMD

  public KafkaStreamsBuilder inputTopic(final String inputTopic) {
    this.inputTopic = inputTopic;
    return this;
  }

  @Override
  protected Topology buildTopology() {
    Objects.requireNonNull(this.inputTopic, "Input topic has not been set.");
    return new TopologyBuilder(this.inputTopic).build();
  }
}
