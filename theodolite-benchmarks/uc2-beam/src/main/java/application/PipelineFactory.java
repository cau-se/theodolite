package application;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;
import java.util.function.Function;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.Duration;
import theodolite.commons.beam.AbstractPipelineFactory;
import theodolite.commons.beam.ConfigurationKeys;
import theodolite.commons.beam.kafka.KafkaActivePowerTimestampReader;
import theodolite.commons.beam.kafka.KafkaWriterTransformation;
import titan.ccp.model.records.ActivePowerRecord;

public class PipelineFactory extends AbstractPipelineFactory {

  public static final String SINK_TYPE_KEY = "sink.type";

  public PipelineFactory(final Configuration configuration) {
    super(configuration);
  }

  @Override
  protected void expandOptions(final PipelineOptions options) {
    // TODO Add for PubSub
    // final String pubSubEmulatorHost = super.config.getString(null);
    // if (pubSubEmulatorHost != null) {
    // final PubsubOptions pubSubOptions = options.as(PubsubOptions.class);
    // pubSubOptions.setPubsubRootUrl("http://" + pubSubEmulatorHost);
    // }
  }

  @Override
  protected void constructPipeline(Pipeline pipeline) {
    final String outputTopic = config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC);

    final Duration duration = Duration.standardMinutes(
        config.getInt(ConfigurationKeys.KAFKA_WINDOW_DURATION_MINUTES));

    final KafkaActivePowerTimestampReader kafkaReader = super.buildKafkaReader();

    // Transform into String
    final StatsToString statsToString = new StatsToString();

    // Write to Kafka
    final String bootstrapServer = config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);
    final KafkaWriterTransformation<String> kafkaWriter =
        new KafkaWriterTransformation<>(bootstrapServer, outputTopic, StringSerializer.class);

    // Apply pipeline transformations
    pipeline.apply(kafkaReader)
        // Apply a fixed window
        .apply(Window.<KV<String, ActivePowerRecord>>into(FixedWindows.of(duration)))
        // Aggregate per window for every key
        .apply(Combine.<String, ActivePowerRecord, Stats>perKey(new StatsAggregation()))
        .setCoder(KvCoder.of(StringUtf8Coder.of(), SerializableCoder.of(Stats.class)))
        // Map into correct output format
        .apply(MapElements.via(statsToString))
        // Write to Kafka
        .apply(kafkaWriter);
  }

  @Override
  protected void registerCoders(CoderRegistry registry) {
    registry.registerCoderForClass(ActivePowerRecord.class,
        AvroCoder.of(ActivePowerRecord.SCHEMA$));
    registry.registerCoderForClass(StatsAggregation.class,
        SerializableCoder.of(StatsAggregation.class));
    registry.registerCoderForClass(StatsAccumulator.class,
        AvroCoder.of(StatsAccumulator.class));
  }

  public static Function<Configuration, AbstractPipelineFactory> factory() {
    return config -> new PipelineFactory(config);
  }

}
