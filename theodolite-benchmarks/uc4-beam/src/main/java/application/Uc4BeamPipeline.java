package application;

import com.google.common.math.StatsAccumulator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.coders.SetCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.windowing.*;
import org.apache.beam.sdk.values.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.joda.time.Duration;
import serialization.*;
import theodolite.commons.beam.AbstractPipeline;
import theodolite.commons.beam.ConfigurationKeys;
import theodolite.commons.beam.kafka.KafkaActivePowerTimestampReader;
import theodolite.commons.beam.kafka.KafkaWriterTransformation;
import titan.ccp.configuration.events.Event;
import titan.ccp.model.records.ActivePowerRecord;
import titan.ccp.model.records.AggregatedActivePowerRecord;

/**
 * Implementation of the use case Database Storage using Apache Beam with the Flink Runner. To
 * execute locally in standalone start Kafka, Zookeeper, the schema-registry and the workload
 * generator using the delayed_startup.sh script. Start a Flink cluster and pass its REST address
 * using--flinkMaster as run parameter. To persist logs add
 * ${workspace_loc:/uc1-application-samza/eclipseConsoleLogs.log} as Output File under Standard
 * Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc4BeamPipeline extends AbstractPipeline {

  protected Uc4BeamPipeline(final PipelineOptions options, final Configuration config) {
    super(options, config);

    // Additional needed variables
    final String feedbackTopic = config.getString(ConfigurationKeys.KAFKA_FEEDBACK_TOPIC);
    final String outputTopic = config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC);
    final String configurationTopic = config.getString(ConfigurationKeys.KAFKA_CONFIGURATION_TOPIC);

    final int windowDurationMinutes = Integer.parseInt(
        config.getString(ConfigurationKeys.KAFKA_WINDOW_DURATION_MINUTES));
    final Duration duration = Duration.standardSeconds(windowDurationMinutes);

    final int triggerInterval = Integer.parseInt(
        config.getString(ConfigurationKeys.TRIGGER_INTERVAL));
    final Duration triggerDelay = Duration.standardSeconds(triggerInterval);

    final int grace = Integer.parseInt(
        config.getString(ConfigurationKeys.GRACE_PERIOD_MS));
    final Duration gracePeriod = Duration.standardSeconds(grace);

    // Build kafka configuration
    final Map<String, Object> consumerConfig = buildConsumerConfig();
    final Map<String, Object> configurationConfig = configurationConfig(config);

    // Set Coders for Classes that will be distributed
    final CoderRegistry cr = this.getCoderRegistry();
    registerCoders(cr);

    // Read from Kafka
    final KafkaActivePowerTimestampReader
        kafkaActivePowerRecordReader =
        new KafkaActivePowerTimestampReader(bootstrapServer, inputTopic, consumerConfig);

    // Transform into AggregatedActivePowerRecords into ActivePowerRecords
    final AggregatedToActive aggregatedToActive = new AggregatedToActive();

    // Write to Kafka
    final KafkaWriterTransformation<AggregatedActivePowerRecord> kafkaOutput =
        new KafkaWriterTransformation<>(
            bootstrapServer, outputTopic, AggregatedActivePowerRecordSerializer.class);

    final KafkaWriterTransformation<AggregatedActivePowerRecord> kafkaFeedback =
        new KafkaWriterTransformation<>(
            bootstrapServer, feedbackTopic, AggregatedActivePowerRecordSerializer.class);


    // Apply pipeline transformations
    // Read from Kafka
    final PCollection<KV<String, ActivePowerRecord>> values = this
        .apply(kafkaActivePowerRecordReader)
        .apply("Read Windows", Window.into(FixedWindows.of(duration)))
        .apply("Set trigger for input", Window
            .<KV<String, ActivePowerRecord>>configure()
            .triggering(Repeatedly.forever(
                AfterProcessingTime.pastFirstElementInPane()
                    .plusDelayOf(triggerDelay)))
            .withAllowedLateness(gracePeriod)
            .discardingFiredPanes());

    // Read the results of earlier aggregations.
    final PCollection<KV<String, ActivePowerRecord>> aggregationsInput = this
        .apply("Read aggregation results", KafkaIO.<String, AggregatedActivePowerRecord>read()
            .withBootstrapServers(bootstrapServer)
            .withTopic(feedbackTopic)
            .withKeyDeserializer(StringDeserializer.class)
            .withValueDeserializer(AggregatedActivePowerRecordDeserializer.class)
            .withTimestampPolicyFactory(
                (tp, previousWaterMark) -> new AggregatedActivePowerRecordEventTimePolicy(
                    previousWaterMark))
            .withoutMetadata())
        .apply("Apply Windows", Window.into(FixedWindows.of(duration)))
        // Convert into the correct data format
        .apply("Convert AggregatedActivePowerRecord to ActivePowerRecord",
            MapElements.via(aggregatedToActive))
        .apply("Set trigger for feedback", Window
            .<KV<String, ActivePowerRecord>>configure()
            .triggering(Repeatedly.forever(
                AfterProcessingTime.pastFirstElementInPane()
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
    final PCollection<KV<String, Set<String>>> configurationStream = this
        .apply("Read sensor groups", KafkaIO.<Event, String>read()
            .withBootstrapServers(bootstrapServer)
            .withTopic(configurationTopic)
            .withKeyDeserializer(EventDeserializer.class)
            .withValueDeserializer(StringDeserializer.class)
            .withConsumerConfigUpdates(configurationConfig)
            .withoutMetadata())
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

    final FilterNullValues filterNullValues = new FilterNullValues();

    // Build pairs of every sensor reading and parent
    final PCollection<KV<SensorParentKey, ActivePowerRecord>> flatMappedValues =
        inputCollection.apply(
                "Duplicate as flatMap",
                ParDo.of(new DuplicateAsFlatMap(childParentPairMap))
                    .withSideInputs(childParentPairMap))
            .apply("Filter only latest changes", Latest.perKey())
            .apply("Filter out null values",
                Filter.by(filterNullValues));

    final SetIdForAggregated setIdForAggregated = new SetIdForAggregated();
    final SetKeyToGroup setKeyToGroup = new SetKeyToGroup();

    // Aggregate for every sensor group of the current level
    final PCollection<KV<String, AggregatedActivePowerRecord>>
        aggregations = flatMappedValues
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

    aggregations
        .apply("Write to feedback topic", kafkaFeedback);

  }


  /**
   * Builds a simple configuration for a Kafka consumer transformation.
   *
   * @return the build configuration.
   */
  public HashMap<String, Object> configurationConfig(final Configuration config) {
    final HashMap<String, Object> consumerConfig = new HashMap<>();
    consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
        config.getString(ConfigurationKeys.ENABLE_AUTO_COMMIT_CONFIG));
    consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        config
            .getString(ConfigurationKeys.AUTO_OFFSET_RESET_CONFIG));

    consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, config
        .getString(ConfigurationKeys.APPLICATION_NAME) + "-configuration");
    return consumerConfig;
  }


  /**
   * Registers all Coders for all needed Coders.
   *
   * @param cr CoderRegistry.
   */
  private static void registerCoders(final CoderRegistry cr) {
    cr.registerCoderForClass(ActivePowerRecord.class,
        AvroCoder.of(ActivePowerRecord.class));
    cr.registerCoderForClass(AggregatedActivePowerRecord.class,
        new AggregatedActivePowerRecordCoder());
    cr.registerCoderForClass(Set.class, SetCoder.of(StringUtf8Coder.of()));
    cr.registerCoderForClass(Event.class, new EventCoder());
    cr.registerCoderForClass(SensorParentKey.class, new SensorParentKeyCoder());
    cr.registerCoderForClass(StatsAccumulator.class, AvroCoder.of(StatsAccumulator.class));
  }
}

