package rocks.theodolite.benchmarks.uc1.kstreams;

import java.util.concurrent.CompletableFuture;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.streams.KafkaStreams;
import titan.ccp.common.configuration.ServiceConfigurations;

/**
 * A microservice that manages the history and, therefore, stores and aggregates incoming
 * measurements.
 *
 */
public class HistoryService {

  private final Configuration config = ServiceConfigurations.createWithDefaults();

  private final CompletableFuture<Void> stopEvent = new CompletableFuture<>();

  /**
   * Start the service.
   */
  public void run() {
    this.createKafkaStreamsApplication();
  }

  /**
   * Build and start the underlying Kafka Streams application of the service.
   *
   */
  private void createKafkaStreamsApplication() {

    final Uc1KafkaStreamsBuilder uc1KafkaStreamsBuilder = new Uc1KafkaStreamsBuilder(this.config);

    final KafkaStreams kafkaStreams = uc1KafkaStreamsBuilder.build();

    this.stopEvent.thenRun(kafkaStreams::close);

    kafkaStreams.start();
  }

  public static void main(final String[] args) {
    new HistoryService().run();
  }

}
