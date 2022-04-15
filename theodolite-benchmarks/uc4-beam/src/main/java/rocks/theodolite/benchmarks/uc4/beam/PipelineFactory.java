package rocks.theodolite.benchmarks.uc4.beam; // NOPMD

import com.google.common.math.StatsAccumulator;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.coders.SetCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.Latest;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Repeatedly;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.joda.time.Duration;
import rocks.theodolite.benchmarks.commons.beam.AbstractPipelineFactory;
import rocks.theodolite.benchmarks.commons.beam.ConfigurationKeys;
import rocks.theodolite.benchmarks.commons.beam.kafka.KafkaActivePowerTimestampReader;
import rocks.theodolite.benchmarks.commons.beam.kafka.KafkaGenericReader;
import rocks.theodolite.benchmarks.commons.beam.kafka.KafkaWriterTransformation;
import rocks.theodolite.benchmarks.uc4.beam.serialization.AggregatedActivePowerRecordCoder;
import rocks.theodolite.benchmarks.uc4.beam.serialization.AggregatedActivePowerRecordDeserializer;
import rocks.theodolite.benchmarks.uc4.beam.serialization.AggregatedActivePowerRecordSerializer;
import rocks.theodolite.benchmarks.uc4.beam.serialization.EventCoder;
import rocks.theodolite.benchmarks.uc4.beam.serialization.EventDeserializer;
import rocks.theodolite.benchmarks.uc4.beam.serialization.SensorParentKeyCoder;
import rocks.theodolite.commons.configuration.events.Event;
import rocks.theodolite.commons.model.records.ActivePowerRecord;
import rocks.theodolite.commons.model.records.AggregatedActivePowerRecord;

/**
 * {@link AbstractPipelineFactory} for UC4.
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
  protected void constructPipeline(final Pipeline pipeline) { // NOPMD
    // Additional needed variables
    final String feedbackTopic = this.config.getString(ConfigurationKeys.KAFKA_FEEDBACK_TOPIC);
    final String outputTopic = this.config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC);
    final String configurationTopic =
        this.config.getString(ConfigurationKeys.KAFKA_CONFIGURATION_TOPIC);

    final Duration duration = Duration.standardSeconds(
        this.config.getInt(ConfigurationKeys.KAFKA_WINDOW_DURATION_MINUTES));
    final Duration triggerDelay = Duration.standardSeconds(
        this.config.getInt(ConfigurationKeys.TRIGGER_INTERVAL));
    final Duration gracePeriod = Duration.standardSeconds(
        this.config.getInt(ConfigurationKeys.GRACE_PERIOD_MS));

    // Read from Kafka
    final String bootstrapServer = this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);

    // ActivePowerRecords
    final KafkaActivePowerTimestampReader kafkaActivePowerRecordReader = super.buildKafkaReader();

    // Configuration Events
    final KafkaGenericReader<Event, String> kafkaConfigurationReader =
        new KafkaGenericReader<>(
            bootstrapServer,
            configurationTopic,
            this.configurationConfig(),
            EventDeserializer.class,
            StringDeserializer.class);

    // Write to Kafka
    final KafkaWriterTransformation<AggregatedActivePowerRecord> kafkaOutput =
        new KafkaWriterTransformation<>(
            bootstrapServer,
            outputTopic,
            AggregatedActivePowerRecordSerializer.class,
            this.buildProducerConfig());

    final KafkaWriterTransformation<AggregatedActivePowerRecord> kafkaFeedback =
        new KafkaWriterTransformation<>(
            bootstrapServer,
            feedbackTopic,
            AggregatedActivePowerRecordSerializer.class,
            this.buildProducerConfig());

    // Apply pipeline transformations
    final PCollection<KV<String, ActivePowerRecord>> values = pipeline
        .apply("Read from Kafka", kafkaActivePowerRecordReader)
        .apply("Read Windows", Window.into(FixedWindows.of(duration)))
        .apply("Set trigger for input", Window
            .<KV<String, ActivePowerRecord>>configure()
            .triggering(Repeatedly.forever(
                AfterProcessingTime.pastFirstElementInPane()
                    .plusDelayOf(triggerDelay)))
            .withAllowedLateness(gracePeriod)
            .discardingFiredPanes());

    // Read the results of earlier aggregations.
    final PCollection<KV<String, ActivePowerRecord>> aggregationsInput = pipeline
        .apply("Read aggregation results", KafkaIO.<String, AggregatedActivePowerRecord>read()
            .withBootstrapServers(bootstrapServer)
            .withTopic(feedbackTopic)
            .withKeyDeserializer(StringDeserializer.class)
            .withValueDeserializerAndCoder(
                AggregatedActivePowerRecordDeserializer.class,
                AvroCoder.of(AggregatedActivePowerRecord.class))
            .withConsumerConfigUpdates(this.buildConsumerConfig())
            .withTimestampPolicyFactory(
                (tp, previousWaterMark) -> new AggregatedActivePowerRecordEventTimePolicy(
                    previousWaterMark))
            .withoutMetadata())
        .apply("Apply Windows", Window.into(FixedWindows.of(duration)))
        // Convert into the correct data format
        .apply("Convert AggregatedActivePowerRecord to ActivePowerRecord",
            MapElements.via(new AggregatedToActive()))
        .apply("Set trigger for feedback", Window
            .<KV<String, ActivePowerRecord>>configure()
            .triggering(Repeatedly.forever(
                AfterProcessingTime
                    .pastFirstElementInPane()
                    .plusDelayOf(triggerDelay)))
            .withAllowedLateness(gracePeriod)
            .discardingFiredPanes());

    // Prepare flatten
    final PCollectionList<KV<String, ActivePowerRecord>> collections =
        PCollectionList.of(values).and(aggregationsInput);

    // Create a single PCollection out of the input and already computed results
    final PCollection<KV<String, ActivePowerRecord>> inputCollection =
        collections.apply("Flatten sensor data and aggregation results",
            Flatten.pCollections());

    // Build the configuration stream from a changelog.
    final PCollection<KV<String, Set<String>>> configurationStream = pipeline
        .apply("Read sensor groups", kafkaConfigurationReader)
        // Only forward relevant changes in the hierarchy
        .apply("Filter changed and status events",
            Filter.by(new FilterEvents()))
        // Build the changelog
        .apply("Generate Parents for every Sensor", ParDo.of(new GenerateParentsFn()))
        .apply("Update child and parent pairs", ParDo.of(new UpdateChildParentPairs()))
        .apply("Set trigger for configuration", Window
            .<KV<String, Set<String>>>configure()
            .triggering(AfterWatermark.pastEndOfWindow()
                .withEarlyFirings(
                    AfterPane.elementCountAtLeast(1)))
            .withAllowedLateness(Duration.ZERO)
            .accumulatingFiredPanes());

    final PCollectionView<Map<String, Set<String>>> childParentPairMap =
        configurationStream.apply(Latest.perKey())
            // Reset trigger to avoid synchronized processing time
            .apply("Reset trigger for configurations", Window
                .<KV<String, Set<String>>>configure()
                .triggering(AfterWatermark.pastEndOfWindow()
                    .withEarlyFirings(
                        AfterPane.elementCountAtLeast(1)))
                .withAllowedLateness(Duration.ZERO)
                .accumulatingFiredPanes())
            .apply(View.asMap());

    // Build pairs of every sensor reading and parent
    final PCollection<KV<SensorParentKey, ActivePowerRecord>> flatMappedValues =
        inputCollection.apply(
            "Duplicate as flatMap",
            ParDo.of(new DuplicateAsFlatMap(childParentPairMap)).withSideInputs(childParentPairMap))
            .apply("Filter only latest changes", Latest.perKey())
            .apply("Filter out null values", Filter.by(new FilterNullValues()));

    final SetIdForAggregated setIdForAggregated = new SetIdForAggregated();
    final SetKeyToGroup setKeyToGroup = new SetKeyToGroup();

    // Aggregate for every sensor group of the current level
    final PCollection<KV<String, AggregatedActivePowerRecord>> aggregations = flatMappedValues
        .apply("Set key to group", MapElements.via(setKeyToGroup))
        // Reset trigger to avoid synchronized processing time
        .apply("Reset trigger for aggregations", Window
            .<KV<String, ActivePowerRecord>>configure()
            .triggering(Repeatedly.forever(
                AfterProcessingTime.pastFirstElementInPane()
                    .plusDelayOf(triggerDelay)))
            .withAllowedLateness(gracePeriod)
            .discardingFiredPanes())
        .apply(
            "Aggregate per group",
            Combine.perKey(new RecordAggregation()))
        .apply("Set the Identifier in AggregatedActivePowerRecord",
            MapElements.via(setIdForAggregated));

    aggregations.apply("Write to aggregation results", kafkaOutput);

    aggregations.apply("Write to feedback topic", kafkaFeedback);
  }

  @Override
  protected void registerCoders(final CoderRegistry registry) {
    registry.registerCoderForClass(
        ActivePowerRecord.class,
        // AvroCoder.of(ActivePowerRecord.SCHEMA$));
        AvroCoder.of(ActivePowerRecord.class, false));
    registry.registerCoderForClass(
        AggregatedActivePowerRecord.class,
        new AggregatedActivePowerRecordCoder());
    registry.registerCoderForClass(
        Set.class,
        SetCoder.of(StringUtf8Coder.of()));
    registry.registerCoderForClass(
        Event.class,
        new EventCoder());
    registry.registerCoderForClass(
        SensorParentKey.class,
        new SensorParentKeyCoder());
    registry.registerCoderForClass(
        StatsAccumulator.class,
        AvroCoder.of(StatsAccumulator.class));
  }


  /**
   * Builds a simple configuration for a Kafka consumer transformation.
   *
   * @return the build configuration.
   */
  private Map<String, Object> configurationConfig() {
    final Map<String, Object> consumerConfig = new HashMap<>();
    consumerConfig.put(
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
        this.config.getString(ConfigurationKeys.ENABLE_AUTO_COMMIT));
    consumerConfig.put(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        this.config.getString(ConfigurationKeys.AUTO_OFFSET_RESET));
    consumerConfig.put(
        ConsumerConfig.GROUP_ID_CONFIG, this.config
            .getString(ConfigurationKeys.APPLICATION_NAME) + "-configuration");
    return consumerConfig;
  }

  private Map<String, Object> buildConsumerConfig() {
    final Map<String, Object> consumerConfig = new HashMap<>();
    consumerConfig.put(
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
        this.config.getString(ConfigurationKeys.ENABLE_AUTO_COMMIT));
    consumerConfig.put(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        this.config.getString(ConfigurationKeys.AUTO_OFFSET_RESET));
    consumerConfig.put(
        AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
        this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL));
    consumerConfig.put(
        KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG,
        this.config.getString(ConfigurationKeys.SPECIFIC_AVRO_READER));
    consumerConfig.put(
        ConsumerConfig.GROUP_ID_CONFIG,
        this.config.getString(ConfigurationKeys.APPLICATION_NAME));
    return consumerConfig;
  }

  /**
   * Builds a simple configuration for a Kafka producer transformation.
   *
   * @return the build configuration.
   */
  private Map<String, Object> buildProducerConfig() {
    final Map<String, Object> config = new HashMap<>();
    config.put(
        AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
        this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL));
    config.put(
        KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG,
        this.config.getString(ConfigurationKeys.SPECIFIC_AVRO_READER));
    return config;
  }

  public static Function<Configuration, AbstractPipelineFactory> factory() {
    return config -> new PipelineFactory(config);
  }

}
