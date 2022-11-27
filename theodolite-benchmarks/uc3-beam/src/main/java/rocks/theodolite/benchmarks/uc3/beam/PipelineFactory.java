package rocks.theodolite.benchmarks.uc3.beam;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;
import java.util.function.Function;
import org.apache.beam.sdk.Pipeline;
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
import rocks.theodolite.benchmarks.commons.beam.AbstractPipelineFactory;
import rocks.theodolite.benchmarks.commons.beam.ConfigurationKeys;
import rocks.theodolite.benchmarks.commons.beam.kafka.KafkaActivePowerTimestampReader;
import rocks.theodolite.benchmarks.commons.beam.kafka.KafkaWriterTransformation;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;

/**
 * {@link AbstractPipelineFactory} for UC3.
 */
public class PipelineFactory extends AbstractPipelineFactory {

  public PipelineFactory(final Configuration configuration) {
    super(configuration);
  }

  @Override
  protected void expandOptions(final PipelineOptions options) {
    // No options to set
  }

  @Override
  protected void constructPipeline(final Pipeline pipeline) {
    final String outputTopic = this.config.getString(Uc3ConfigurationKeys.KAFKA_OUTPUT_TOPIC);

    final Duration duration =
        Duration.standardDays(this.config.getInt(Uc3ConfigurationKeys.AGGREGATION_DURATION_DAYS));
    final Duration aggregationAdvanceDuration =
        Duration.standardDays(this.config.getInt(Uc3ConfigurationKeys.AGGREGATION_ADVANCE_DAYS));
    final Duration triggerDelay =
        Duration.standardSeconds(this.config.getInt(Uc3ConfigurationKeys.TRIGGER_INTERVAL_SECONDS));

    // Read from Kafka
    final KafkaActivePowerTimestampReader kafkaReader = super.buildKafkaReader();

    // Map the time format
    final MapTimeFormat mapTimeFormat = new MapTimeFormat();

    // Get the stats per HourOfDay
    final HourOfDayWithStats hourOfDayWithStats = new HourOfDayWithStats();

    // Write to Kafka
    final String bootstrapServer = this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);
    final KafkaWriterTransformation<String> kafkaWriter =
        new KafkaWriterTransformation<>(bootstrapServer, outputTopic, StringSerializer.class);

    pipeline.apply(kafkaReader)
        // Map to correct time format
        .apply(MapElements.via(mapTimeFormat))
        // Apply a sliding window
        .apply(Window
            .<KV<HourOfDayKey, ActivePowerRecord>>into(
                SlidingWindows.of(duration).every(aggregationAdvanceDuration))
            .triggering(AfterWatermark.pastEndOfWindow()
                .withEarlyFirings(
                    AfterProcessingTime.pastFirstElementInPane().plusDelayOf(triggerDelay)))
            .withAllowedLateness(Duration.ZERO)
            .accumulatingFiredPanes())

        // Aggregate per window for every key
        .apply(Combine.perKey(new StatsAggregation()))
        .setCoder(KvCoder.of(new HourOfDayKeyCoder(), SerializableCoder.of(Stats.class)))

        // Map into correct output format
        .apply(MapElements.via(hourOfDayWithStats))
        // Write to Kafka
        .apply(kafkaWriter);
  }

  @Override
  protected void registerCoders(final CoderRegistry registry) {
    registry.registerCoderForClass(
        ActivePowerRecord.class,
        // AvroCoder.of(ActivePowerRecord.SCHEMA$));
        AvroCoder.of(ActivePowerRecord.class, false));
    registry.registerCoderForClass(
        HourOfDayKey.class,
        new HourOfDayKeyCoder());
    registry.registerCoderForClass(
        StatsAggregation.class,
        SerializableCoder.of(StatsAggregation.class));
    registry.registerCoderForClass(
        StatsAccumulator.class,
        AvroCoder.of(StatsAccumulator.class));
  }

  public static Function<Configuration, AbstractPipelineFactory> factory() {
    return config -> new PipelineFactory(config);
  }

}
