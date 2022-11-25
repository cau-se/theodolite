package rocks.theodolite.benchmarks.uc3.hazelcastjet;

import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.ServiceFactories;
import com.hazelcast.jet.pipeline.ServiceFactory;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.jet.pipeline.WindowDefinition;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import rocks.theodolite.benchmarks.commons.hazelcastjet.PipelineFactory;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;


/**
 * PipelineFactory for use case 3. Allows to build and extend pipelines.
 */
public class Uc3PipelineFactory extends PipelineFactory {

  private final Duration hoppingSize;
  private final Duration windowSize;
  private final Duration emitPeriod;

  /**
   * Build a new Pipeline.
   *
   * @param kafkaReadPropsForPipeline Properties Object containing the necessary kafka reads
   *        attributes.
   * @param kafkaWritePropsForPipeline Properties Object containing the necessary kafka write
   *        attributes.
   * @param kafkaInputTopic The name of the input topic used for the pipeline.
   * @param kafkaOutputTopic The name of the output topic used for the pipeline.
   * @param hoppingSize The hop length of the sliding window used in the aggregation of this
   *        pipeline.
   * @param windowSize The window length of the sliding window used in the aggregation of this
   *        pipeline.
   */
  public Uc3PipelineFactory(final Properties kafkaReadPropsForPipeline,
      final String kafkaInputTopic,
      final Properties kafkaWritePropsForPipeline,
      final String kafkaOutputTopic,
      final Duration windowSize,
      final Duration hoppingSize,
      final Duration emitPeriod) {
    super(
        kafkaReadPropsForPipeline,
        kafkaInputTopic,
        kafkaWritePropsForPipeline,
        kafkaOutputTopic);
    this.windowSize = windowSize;
    this.hoppingSize = hoppingSize;
    this.emitPeriod = emitPeriod;
  }

  /**
   * Builds a pipeline which can be used for stream processing using Hazelcast Jet.
   *
   * @return a pipeline used which can be used in a Hazelcast Jet Instance to process data for UC3.
   */
  @Override
  public Pipeline buildPipeline() {

    // Define the source
    final StreamSource<Map.Entry<String, ActivePowerRecord>> kafkaSource = KafkaSources
        .<String, ActivePowerRecord>kafka(
            this.kafkaReadPropsForPipeline, this.kafkaInputTopic);

    // Extend topology for UC3
    final StreamStage<Map.Entry<String, String>> uc3Product =
        this.extendUc3Topology(kafkaSource);

    // Add Sink1: Logger
    uc3Product.writeTo(Sinks.logger());
    // Add Sink2: Write back to kafka for the final benchmark
    uc3Product.writeTo(KafkaSinks.<String, String>kafka(
        this.kafkaWritePropsForPipeline, this.kafkaOutputTopic));

    return this.pipe;
  }

  /**
   * Extends to a blank Hazelcast Jet Pipeline the UC3 topology defined by theodolite.
   *
   * <p>
   * UC3 takes {@code ActivePowerRecord} object, groups them by keys and calculates average double
   * values for a sliding window and sorts them into the hour of the day.
   * </p>
   *
   * @param source A streaming source to fetch data from.
   * @return A {@code StreamStage<Map.Entry<String,String>>} with the above definition of the key
   *         and value of the Entry object. It can be used to be further modified or directly be
   *         written into a sink.
   */
  public StreamStage<Map.Entry<String, String>> extendUc3Topology(
      final StreamSource<Map.Entry<String, ActivePowerRecord>> source) {

    final ServiceFactory<?, MapTimeKeyConfiguration> timeKeyConfigService =
        ServiceFactories.nonSharedService(
            pctx -> new MapTimeKeyConfiguration(
                new HourOfDayKeyFactory(),
                ZoneId.of("Europe/Paris") // TODO Make configurable
            ));

    // Build the pipeline topology.
    return this.pipe
        .readFrom(source)
        // use Timestamps
        .withNativeTimestamps(0)
        // .setLocalParallelism(1)
        // Map key to HourOfDayKey
        .mapUsingService(timeKeyConfigService, (config, record) -> {
          final String sensorId = record.getValue().getIdentifier();
          final Instant instant = Instant.ofEpochMilli(record.getValue().getTimestamp());
          final LocalDateTime dateTime = LocalDateTime.ofInstant(instant, config.getZone());
          final HourOfDayKey key = config.getKeyFactory().createKey(sensorId, dateTime);
          return Map.entry(key, record.getValue());
        })
        // group by new keys
        .groupingKey(Entry::getKey)
        // Sliding/Hopping Window
        .window(WindowDefinition
            .sliding(this.windowSize.toMillis(), this.hoppingSize.toMillis())
            .setEarlyResultsPeriod(this.emitPeriod.toMillis()))
        // get aggregated values for (sensoreId, hourOfDay)
        .aggregate(StatsAggregatorFactory.create())
        // map to return pair sensorID -> stats
        .map(agg -> {
          final String sensorId = agg.getKey().getSensorId();
          final String stats = agg.getValue().toString();
          return Map.entry(sensorId, stats);
        });
  }
}
