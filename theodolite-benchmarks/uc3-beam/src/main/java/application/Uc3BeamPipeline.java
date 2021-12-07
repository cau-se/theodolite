package application;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;
import java.util.Map;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.Duration;
import theodolite.commons.beam.AbstractPipeline;
import theodolite.commons.beam.ConfigurationKeys;
import theodolite.commons.beam.kafka.KafkaActivePowerTimestampReader;
import theodolite.commons.beam.kafka.KafkaWriterTransformation;
import titan.ccp.model.records.ActivePowerRecord;


/**
 * Implementation of the use case Database Storage using Apache Beam with the Flink Runner. To
 * execute locally in standalone start Kafka, Zookeeper, the schema-registry and the workload
 * generator using the delayed_startup.sh script. Start a Flink cluster and pass its REST adress
 * using--flinkMaster as run parameter. To persist logs add
 * ${workspace_loc:/uc1-application-samza/eclipseConsoleLogs.log} as Output File under Standard
 * Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc3BeamPipeline extends AbstractPipeline {

  protected Uc3BeamPipeline(final PipelineOptions options, final Configuration config) {
    super(options, config);
    // Additional needed variables
    final String outputTopic = config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC);

    final int windowDurationDays = Integer.parseInt(
        config.getString(ConfigurationKeys.AGGREGATION_DURATION_DAYS));
    final Duration duration = Duration.standardDays(windowDurationDays);

    final int aggregationAdvance = Integer.parseInt(
        config.getString(ConfigurationKeys.AGGREGATION_ADVANCE_DAYS));
    final Duration aggregationAdvanceDuration = Duration.standardDays(aggregationAdvance);

    final int triggerInterval = Integer.parseInt(
        config.getString(ConfigurationKeys.TRIGGER_INTERVAL));
    final Duration triggerDelay = Duration.standardSeconds(triggerInterval);

    // Build kafka configuration
    final Map consumerConfig = buildConsumerConfig();

    // Set Coders for Classes that will be distributed
    final CoderRegistry cr = this.getCoderRegistry();
    registerCoders(cr);

    // Read from Kafka
    final KafkaActivePowerTimestampReader kafka =
        new KafkaActivePowerTimestampReader(bootstrapServer, inputTopic, consumerConfig);

    // Map the time format
    final MapTimeFormat mapTimeFormat = new MapTimeFormat();

    // get the stats per HourOfDay
    final HourOfDayWithStats hourOfDayWithStats = new HourOfDayWithStats();

    // Write to Kafka
    final KafkaWriterTransformation<String> kafkaWriter =
        new KafkaWriterTransformation<>(bootstrapServer, outputTopic, StringSerializer.class);

    this.apply(kafka)
        // Map to correct time format
        .apply(MapElements.via(mapTimeFormat))
        // Apply a sliding window
        .apply(Window
            .<KV<HourOfDayKey, ActivePowerRecord>>
                into(SlidingWindows.of(duration).every(aggregationAdvanceDuration))
            .triggering(AfterWatermark.pastEndOfWindow()
                .withEarlyFirings(
                    AfterProcessingTime.pastFirstElementInPane().plusDelayOf(triggerDelay)))
            .withAllowedLateness(Duration.ZERO)
            .accumulatingFiredPanes())

        // Aggregate per window for every key
        .apply(Combine.<HourOfDayKey, ActivePowerRecord, Stats>perKey(
            new StatsAggregation()))
        .setCoder(KvCoder.of(new HourOfDaykeyCoder(), SerializableCoder.of(Stats.class)))

        // Map into correct output format
        .apply(MapElements
            .via(hourOfDayWithStats))
        // Write to Kafka
        .apply(kafkaWriter);
  }


  /**
   * Registers all Coders for all needed Coders.
   *
   * @param cr CoderRegistry.
   */
  private static void registerCoders(final CoderRegistry cr) {
    cr.registerCoderForClass(ActivePowerRecord.class, AvroCoder.of(ActivePowerRecord.SCHEMA$));
    cr.registerCoderForClass(HourOfDayKey.class, new HourOfDaykeyCoder());
    cr.registerCoderForClass(StatsAggregation.class,
        SerializableCoder.of(StatsAggregation.class));
    cr.registerCoderForClass(StatsAccumulator.class, AvroCoder.of(StatsAccumulator.class));
  }
}

