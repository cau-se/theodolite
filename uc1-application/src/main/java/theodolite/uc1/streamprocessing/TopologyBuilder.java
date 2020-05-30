package theodolite.uc1.streamprocessing;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.common.kafka.avro.SchemaRegistryAvroSerdeFactory;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Builds Kafka Stream Topology for the History microservice.
 */
public class TopologyBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopologyBuilder.class);

  private final String inputTopic;
  private final SchemaRegistryAvroSerdeFactory srAvroSerdeFactory;

  private final Gson gson = new Gson();
  private final StreamsBuilder builder = new StreamsBuilder();


  /**
   * Create a new {@link TopologyBuilder} using the given topics.
   */
  public TopologyBuilder(final String inputTopic,
      final SchemaRegistryAvroSerdeFactory srAvroSerdeFactory) {
    this.inputTopic = inputTopic;
    this.srAvroSerdeFactory = srAvroSerdeFactory;
  }

  /**
   * Build the {@link Topology} for the History microservice.
   */
  public Topology build() {
    this.builder
        .stream(this.inputTopic, Consumed.with(
            Serdes.String(),
            this.srAvroSerdeFactory.<ActivePowerRecord>forKeys()))
        .mapValues(v -> this.gson.toJson(v))
        .foreach((k, v) -> LOGGER.info("Key: " + k + " Value: " + v));

    return this.builder.build();
  }
}
