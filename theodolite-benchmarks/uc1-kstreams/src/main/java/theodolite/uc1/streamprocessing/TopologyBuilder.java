package theodolite.uc1.streamprocessing;

import com.google.gson.Gson;
import java.util.Properties;
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
  private static final Gson GSON = new Gson();

  private final String inputTopic;
  private final SchemaRegistryAvroSerdeFactory srAvroSerdeFactory;

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
  public Topology build(final Properties properties) {
    this.builder
        .stream(this.inputTopic, Consumed.with(
            Serdes.String(),
            this.srAvroSerdeFactory.<ActivePowerRecord>forValues()))
        .mapValues(v -> GSON.toJson(v))
        .foreach((k, record) -> LOGGER.info("Record: {}", record));

    return this.builder.build(properties);
  }
}
