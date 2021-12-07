package theodolite.uc3.application;

import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.jet.pipeline.WindowDefinition;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import theodolite.uc3.application.uc3specifics.HourOfDayKey;
import theodolite.uc3.application.uc3specifics.HoursOfDayKeyFactory;
import theodolite.uc3.application.uc3specifics.StatsKeyFactory;
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

    // Build Pipeline for the History Service of UC3
    final Pipeline pipe = Pipeline.create();
    final StreamStage<Map.Entry<String, String>> mapProduct =
        pipe
            .readFrom(KafkaSources
                .<String, ActivePowerRecord>kafka(
                    kafkaReadPropsForPipeline, kafkaInputTopic))
            // use Timestamps
            .withNativeTimestamps(0)
            .setLocalParallelism(1)
            // Map timestamp to hour of day and create new key using sensorID and
            // datetime mapped to HourOfDay
            .map(record -> {
              String sensorId = record.getValue().getIdentifier();
              long timestamp = record.getValue().getTimestamp();
              LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                  TimeZone.getDefault().toZoneId());

              final StatsKeyFactory<HourOfDayKey> keyFactory = new HoursOfDayKeyFactory();
              HourOfDayKey newKey = keyFactory.createKey(sensorId, dateTime);

              return Map.entry(newKey, record.getValue());
            })
            // group by new keys
            .groupingKey(newRecord -> newRecord.getKey())
            // Sliding/Hopping Window
            .window(WindowDefinition.sliding(TimeUnit.SECONDS.toMillis(windowSizeInSeconds),
                TimeUnit.SECONDS.toMillis(hoppingSizeInSeconds)))
            // get average value of group (sensoreId,hourOfDay)
            .aggregate(
                AggregateOperations.averagingDouble(record -> record.getValue().getValueInW()))
            // map to return pair (sensorID,hourOfDay) -> (averaged what value)
            .map(agg -> {
              String theValue = agg.getValue().toString();
              String theKey = agg.getKey().toString();
              return Map.entry(theKey, theValue);
            });
    // Add Sink1: Logger
    mapProduct.writeTo(Sinks.logger());
    // Add Sink2: Write back to kafka for the final benchmark
    mapProduct.writeTo(KafkaSinks.<String, String>kafka(
        kafkaWritePropsForPipeline, kafkaOutputTopic));

    return pipe;
  }

}
