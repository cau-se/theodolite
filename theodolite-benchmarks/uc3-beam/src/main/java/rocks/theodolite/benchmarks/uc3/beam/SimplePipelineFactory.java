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
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.commons.configuration2.Configuration;
import org.joda.time.Duration;
import rocks.theodolite.benchmarks.commons.beam.AbstractPipelineFactory;
import rocks.theodolite.benchmarks.commons.beam.ConfigurationKeys;
import rocks.theodolite.benchmarks.commons.beam.kafka.KafkaActivePowerTimestampReader;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * {@link AbstractPipelineFactory} for UC3.
 */
public class SimplePipelineFactory extends AbstractPipelineFactory {

  public SimplePipelineFactory(final Configuration configuration) {
    super(configuration);
  }

  @Override
  protected void expandOptions(final PipelineOptions options) {
    // No options to set
  }

  @Override
  protected void constructPipeline(final Pipeline pipeline) {
    final Duration duration =
        Duration
            .standardSeconds(this.config.getInt(ConfigurationKeys.AGGREGATION_DURATION_SECONDS));
    final Duration aggregationAdvanceDuration =
        Duration.standardSeconds(this.config.getInt(ConfigurationKeys.AGGREGATION_ADVANCE_SECONDS));

    System.out.println(duration);
    System.out.println(aggregationAdvanceDuration);

    // Read from Kafka
    // TODO allow for pubsub
    final KafkaActivePowerTimestampReader kafkaReader = super.buildKafkaReader();

    // Map the time format
    final MapTimeFormat mapTimeFormat = new MapTimeFormat();

    // Get the stats per HourOfDay
    final HourOfDayWithStats hourOfDayWithStats = new HourOfDayWithStats();

    pipeline.apply(kafkaReader)
        // Map to correct time format
        // TODO optional
        .apply(MapElements.via(mapTimeFormat))
        // Apply a sliding window
        .apply(Window
            .<KV<HourOfDayKey, ActivePowerRecord>>into(
                SlidingWindows
                    .of(duration)
                    .every(aggregationAdvanceDuration)))
        // .triggering(AfterWatermark.pastEndOfWindow())
        // .withAllowedLateness(Duration.ZERO))

        // Aggregate per window for every key
        .apply(Combine.perKey(new StatsAggregation()))
        .setCoder(KvCoder.of(new HourOfDayKeyCoder(), SerializableCoder.of(Stats.class)))
        .apply(MapElements.via(hourOfDayWithStats)) // TODO decide what to do
        .apply(ParDo.of(new LogWriter<>()));
  }

  @Override
  protected void registerCoders(final CoderRegistry registry) {
    registry.registerCoderForClass(
        ActivePowerRecord.class,
        AvroCoder.of(ActivePowerRecord.SCHEMA$));
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
    return config -> new SimplePipelineFactory(config);
  }

}
