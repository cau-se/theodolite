package rocks.theodolite.benchmarks.uc3.hazelcastjet;

import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.jet.pipeline.WindowDefinition;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import rocks.theodolite.benchmarks.uc3.hazelcastjet.uc3specifics.HourOfDayKey;
import rocks.theodolite.benchmarks.uc3.hazelcastjet.uc3specifics.HoursOfDayKeyFactory;
import rocks.theodolite.benchmarks.uc3.hazelcastjet.uc3specifics.StatsKeyFactory;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Builder to build a HazelcastJet Pipeline for UC3 which can be used for stream processing using
 * Hazelcast Jet.
 */
public class Uc3PipelineBuilder {

  /**
   * Builds a pipeline which can be used for stream processing using Hazelcast Jet.
   *
   * @param kafkaReadPropsForPipeline Properties Object containing the necessary kafka reads
   *        attributes.
   * @param kafkaWritePropsForPipeline Properties Object containing the necessary kafka write
   *        attributes.
   * @param kafkaInputTopic The name of the input topic used for the pipeline.
   * @param kafkaOutputTopic The name of the output topic used for the pipeline.
   * @param hoppingSizeInSeconds The hop length of the sliding window used in the aggregation of
   *        this pipeline.
   * @param windowSizeInSeconds The window length of the sliding window used in the aggregation of
   *        this pipeline.
   * @return returns a Pipeline used which can be used in a Hazelcast Jet Instance to process data
   *         for UC3.
   */
  public Pipeline build(final Properties kafkaReadPropsForPipeline,
      final Properties kafkaWritePropsForPipeline, final String kafkaInputTopic,
      final String kafkaOutputTopic,
      final int hoppingSizeInSeconds, final int windowSizeInSeconds) {

    // Define a new Pipeline
    final Pipeline pipe = Pipeline.create();

    // Define the source
    final StreamSource<Entry<String, ActivePowerRecord>> kafkaSource = KafkaSources
        .<String, ActivePowerRecord>kafka(
            kafkaReadPropsForPipeline, kafkaInputTopic);

    // Extend topology for UC3
    final StreamStage<Map.Entry<String, String>> uc3Product =
        this.extendUc3Topology(pipe, kafkaSource, hoppingSizeInSeconds, windowSizeInSeconds);

    // Add Sink1: Logger
    uc3Product.writeTo(Sinks.logger());
    // Add Sink2: Write back to kafka for the final benchmark
    uc3Product.writeTo(KafkaSinks.<String, String>kafka(
        kafkaWritePropsForPipeline, kafkaOutputTopic));

    return pipe;
  }

  /**
   * Extends to a blank Hazelcast Jet Pipeline the UC3 topology defined by theodolite.
   *
   * <p>
   * UC3 takes {@code ActivePowerRecord} object, groups them by keys and calculates average double
   * values for a sliding window and sorts them into the hour of the day.
   * </p>
   *
   * @param pipe The blank hazelcast jet pipeline to extend the logic to.
   * @param source A streaming source to fetch data from.
   * @param hoppingSizeInSeconds The jump distance of the "sliding" window.
   * @param windowSizeInSeconds The size of the "sliding" window.
   * @return A {@code StreamStage<Map.Entry<String,String>>} with the above definition of the key
   *         and value of the Entry object. It can be used to be further modified or directly be
   *         written into a sink.
   */
  public StreamStage<Map.Entry<String, String>> extendUc3Topology(final Pipeline pipe,
      final StreamSource<Entry<String, ActivePowerRecord>> source, final int hoppingSizeInSeconds,
      final int windowSizeInSeconds) {
    // Build the pipeline topology.
    return pipe
        .readFrom(source)
        // use Timestamps
        .withNativeTimestamps(0)
        .setLocalParallelism(1)
        // Map timestamp to hour of day and create new key using sensorID and
        // datetime mapped to HourOfDay
        .map(record -> {
          final String sensorId = record.getValue().getIdentifier();
          final long timestamp = record.getValue().getTimestamp();
          final LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
              TimeZone.getDefault().toZoneId());

          final StatsKeyFactory<HourOfDayKey> keyFactory = new HoursOfDayKeyFactory();
          final HourOfDayKey newKey = keyFactory.createKey(sensorId, dateTime);

          return Map.entry(newKey, record.getValue());
        })
        // group by new keys
        .groupingKey(Entry::getKey)
        // Sliding/Hopping Window
        .window(WindowDefinition.sliding(TimeUnit.DAYS.toMillis(windowSizeInSeconds),
            TimeUnit.DAYS.toMillis(hoppingSizeInSeconds)))
        // get average value of group (sensoreId,hourOfDay)
        .aggregate(
            AggregateOperations.averagingDouble(record -> record.getValue().getValueInW()))
        // map to return pair (sensorID,hourOfDay) -> (averaged what value)
        .map(agg -> {
          final String theValue = agg.getValue().toString();
          final String theKey = agg.getKey().toString();
          return Map.entry(theKey, theValue);
        });
  }

}
