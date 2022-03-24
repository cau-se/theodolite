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
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.configuration2.Configuration;
import org.joda.time.Duration;
import rocks.theodolite.benchmarks.commons.beam.AbstractPipelineFactory;
import rocks.theodolite.benchmarks.commons.beam.ConfigurationKeys;
import rocks.theodolite.benchmarks.commons.beam.kafka.KafkaActivePowerTimestampReader;
import rocks.theodolite.benchmarks.uc3.beam.pubsub.PubSubSource;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * {@link AbstractPipelineFactory} for UC3.
 */
public class SimplePipelineFactory extends AbstractPipelineFactory {

  public static final String SOURCE_TYPE_KEY = "source.type";

  public static final String PUBSSUB_SOURCE_PROJECT_KEY = "source.pubsub.project";
  public static final String PUBSSUB_SOURCE_TOPIC_KEY = "source.pubsub.topic";
  public static final String PUBSSUB_SOURCE_SUBSCR_KEY = "source.pubsub.subscription";

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

    final String sourceType = this.config.getString(SOURCE_TYPE_KEY);

    PCollection<ActivePowerRecord> activePowerRecords;

    if ("pubsub".equals(sourceType)) {
      final String project = this.config.getString(PUBSSUB_SOURCE_PROJECT_KEY);
      final String topic = this.config.getString(PUBSSUB_SOURCE_TOPIC_KEY);
      final String subscription = this.config.getString(PUBSSUB_SOURCE_SUBSCR_KEY);
      // Read messages from Pub/Sub and encode them as Avro records
      if (subscription == null) {
        activePowerRecords = pipeline.apply(PubSubSource.forTopic(topic, project));
      } else {
        activePowerRecords = pipeline.apply(PubSubSource.forSubscription(project, subscription));
      }
    } else {
      final KafkaActivePowerTimestampReader kafka = super.buildKafkaReader();
      // Read messages from Kafka as Avro records and drop keys
      activePowerRecords = pipeline.apply(kafka).apply(Values.create());
    }

    // Map the time format
    final MapTimeFormat mapTimeFormat = new MapTimeFormat();

    // Get the stats per HourOfDay
    final HourOfDayWithStats hourOfDayWithStats = new HourOfDayWithStats();

    activePowerRecords
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
