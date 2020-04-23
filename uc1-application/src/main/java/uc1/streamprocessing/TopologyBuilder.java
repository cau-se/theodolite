package uc1.streamprocessing;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.common.kieker.kafka.IMonitoringRecordSerde;
import titan.ccp.models.records.ActivePowerRecordFactory;

/**
 * Builds Kafka Stream Topology for the History microservice.
 */
public class TopologyBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopologyBuilder.class);

  private final String inputTopic;
  private final Gson gson;

  private final StreamsBuilder builder = new StreamsBuilder();

  /**
   * Create a new {@link TopologyBuilder} using the given topics.
   */
  public TopologyBuilder(final String inputTopic) {
    this.inputTopic = inputTopic;
    this.gson = new Gson();
  }

  /**
   * Build the {@link Topology} for the History microservice.
   */
  public Topology build() {

    this.builder
        .stream(this.inputTopic, Consumed.with(
            Serdes.String(),
            IMonitoringRecordSerde.serde(new ActivePowerRecordFactory())))
        .mapValues(v -> this.gson.toJson(v))
        .foreach((k, v) -> LOGGER.info("Key: " + k + " Value: " + v));

    return this.builder.build();
  }
}
