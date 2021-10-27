package application;

import com.google.common.base.MoreObjects;
import com.google.common.math.StatsAccumulator;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.beam.runners.samza.SamzaRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.coders.NullableCoder;
import org.apache.beam.sdk.coders.SetCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.state.StateSpec;
import org.apache.beam.sdk.state.StateSpecs;
import org.apache.beam.sdk.state.ValueState;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.Latest;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.transforms.windowing.AfterPane;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Repeatedly;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.Duration;
import serialization.AggregatedActivePowerRecordCoder;
import serialization.AggregatedActivePowerRecordDeserializer;
import serialization.AggregatedActivePowerRecordSerializer;
import serialization.EventCoder;
import serialization.EventDeserializer;
import serialization.SensorParentKeyCoder;
import titan.ccp.configuration.events.Event;
import titan.ccp.model.records.ActivePowerRecord;
import titan.ccp.model.records.AggregatedActivePowerRecord;

/**
 * Implementation of the use case Hierarchical Aggregation using Apache Beam with the Samza
 * Runner. To run locally in standalone start Kafka, Zookeeper, the schema-registry and the
 * workload generator using the delayed_startup.sh script. Add
 * --configFactory=org.apache.samza.config.factories.PropertiesConfigFactory
 * --configFilePath=${workspace_loc:uc4-application-samza}/config/standalone_local.properties
 * --samzaExecutionEnvironment=STANDALONE --maxSourceParallelism=1024 --as program arguments. To
 * persist logs add ${workspace_loc:/uc4-application-samza/eclipseConsoleLogs.log} as Output File
 * under Standard Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public class Uc4ApplicationBeam {
  private static final String JOB_NAME = "Uc4Application";
  private static final String BOOTSTRAP = "KAFKA_BOOTSTRAP_SERVERS";
  private static final String INPUT = "INPUT";
  private static final String OUTPUT = "OUTPUT";
  private static final String CONFIGURATION = "CONFIGURATION";
  private static final String FEEDBACKTOPIC = "FEEDBACKTOPIC";
  private static final String SCHEMA_REGISTRY = "SCHEMA_REGISTRY_URL";
  private static final String YES = "true";
  private static final String USE_AVRO_READER = YES;
  private static final String AUTO_COMMIT_CONFIG = YES;
  private static final String KAFKA_WINDOW_DURATION  = "KAFKA_WINDOW_DURATION";
  private static final String TRIGGER_INTERVAL  = "TRIGGER_INTERVAL";
  private static final String GRACE_PERIOD  = "GRACE_PERIOD";
  private static final String AUTO_OFFSET_RESET_CONFIG  = "earliest";




  /**
   * Private constructor to avoid instantiation.
   */
  private Uc4ApplicationBeam() {
    throw new UnsupportedOperationException();
  }

  /**
   * Start executing this microservice.
   */
  @SuppressWarnings({"serial", "unchecked", "rawtypes"})
  public static void main(final String[] args) {

    // Set Configuration for Windows
    final int windowDuration = Integer.parseInt(
        System.getenv(KAFKA_WINDOW_DURATION) == null
            ? "60" : System.getenv(KAFKA_WINDOW_DURATION));
    final Duration duration = Duration.standardSeconds(windowDuration);
    final int triggerInterval = Integer.parseInt(
        System.getenv(TRIGGER_INTERVAL) == null
            ? "30" : System.getenv(TRIGGER_INTERVAL));

    final Duration triggerDelay = Duration.standardSeconds(triggerInterval);

    final int grace = Integer.parseInt(
        System.getenv(GRACE_PERIOD) == null
            ? "270" : System.getenv(GRACE_PERIOD));

    final Duration gracePeriod = Duration.standardSeconds(grace);
    // Set Configuration for Kafka
    final String bootstrapServer =
        System.getenv(BOOTSTRAP) == null ? "my-confluent-cp-kafka:9092"
            : System.getenv(BOOTSTRAP);
    final String inputTopic = System.getenv(INPUT) == null ? "input" : System.getenv(INPUT);
    final String outputTopic = System.getenv(OUTPUT) == null ? "output" : System.getenv(OUTPUT);
    final String configurationTopic =
        System.getenv(CONFIGURATION) == null ? "configuration" : System.getenv(CONFIGURATION);
    final String feedbackTopic =
        System.getenv(FEEDBACKTOPIC) == null ? "aggregation-feedback"
            : System.getenv(FEEDBACKTOPIC);
    final String schemaRegistryUrl =
        System.getenv(SCHEMA_REGISTRY) == null ? "http://my-confluent-cp-schema-registry:8081"
            : System.getenv(SCHEMA_REGISTRY);


    // final String inputTopic = "input";
    // final String outputTopic = "output";
    // final String bootstrapServer = "localhost:9092";
    // final String configurationTopic = "configuration";
    // final String feedbackTopic = "aggregation-feedback";
    // final String schemaRegistryURL = "http://localhost:8081";
    // final Duration duration = Duration.standardSeconds(5);
    // final Duration gracePeriod = Duration.standardMinutes(5);
    // final Duration triggerDelay = Duration.standardSeconds(5);

    // Set consumer configuration for the schema registry and commits back to Kafka
    final HashMap<String, Object> consumerConfig = new HashMap<>();
    consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, AUTO_COMMIT_CONFIG);
    consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET_CONFIG);
    consumerConfig.put("schema.registry.url", schemaRegistryUrl);
    consumerConfig.put("specific.avro.reader", USE_AVRO_READER);
    consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "uc-application-input");

    final HashMap<String, Object> consumerConfigConfiguration = new HashMap<>();
    consumerConfigConfiguration.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, AUTO_COMMIT_CONFIG);
    consumerConfigConfiguration.put(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET_CONFIG);
    consumerConfigConfiguration.put(ConsumerConfig.GROUP_ID_CONFIG, "uc-application-configuration");

    // Create run options from args
    final PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
    options.setRunner(SamzaRunner.class);
    options.setJobName(JOB_NAME);

    final Pipeline pipeline = Pipeline.create(options);
    final CoderRegistry cr = pipeline.getCoderRegistry();

    // Set Coders for Classes that will be distributed
    cr.registerCoderForClass(ActivePowerRecord.class,
        NullableCoder.of(AvroCoder.of(ActivePowerRecord.class)));
    cr.registerCoderForClass(AggregatedActivePowerRecord.class,
        new AggregatedActivePowerRecordCoder());
    cr.registerCoderForClass(Set.class, SetCoder.of(StringUtf8Coder.of()));
    cr.registerCoderForClass(Event.class, new EventCoder());
    cr.registerCoderForClass(SensorParentKey.class, new SensorParentKeyCoder());
    cr.registerCoderForClass(StatsAccumulator.class, AvroCoder.of(StatsAccumulator.class));


    @SuppressWarnings({"unchecked", "rawtypes"})
    final PTransform<PBegin, PCollection<KV<String, ActivePowerRecord>>> kafka =
        KafkaIO.<String, ActivePowerRecord>read()
            .withBootstrapServers(bootstrapServer)
            .withTopic(inputTopic)
            .withKeyDeserializer(StringDeserializer.class)
            .withValueDeserializerAndCoder((Class) KafkaAvroDeserializer.class,
                NullableCoder.of(AvroCoder.of(ActivePowerRecord.class)))
            .withConsumerConfigUpdates(consumerConfig)
            // Set TimeStampPolicy for event time
            .withTimestampPolicyFactory(
                (tp, previousWaterMark) -> new EventTimePolicy(previousWaterMark))
            .withoutMetadata();

    // Apply pipeline transformations
    // Read from Kafka
    final PCollection<KV<String, ActivePowerRecord>> values = pipeline.apply(kafka)
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
            .withValueDeserializer(AggregatedActivePowerRecordDeserializer.class)
            .withTimestampPolicyFactory(
                (tp, previousWaterMark) -> new AggregatedActivePowerRecordEventTimePolicy(
                          previousWaterMark))
            .withoutMetadata())
        .apply("Apply Winddows", Window.into(FixedWindows.of(duration)))
        // Convert into the correct data format
        .apply("Convert AggregatedActivePowerRecord to ActivePowerRecord",
            MapElements.via(
                new SimpleFunction<KV<String, AggregatedActivePowerRecord>,
                    KV<String, ActivePowerRecord>>() {
                @Override
                public KV<String, ActivePowerRecord> apply(
                    final KV<String, AggregatedActivePowerRecord> kv) {
                      return KV.of(kv.getKey(), new ActivePowerRecord(kv.getValue().getIdentifier(),
                            kv.getValue().getTimestamp(), kv.getValue().getSumInW()));
                }
                }))
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
    final PCollection<KV<String, Set<String>>> configurationStream = pipeline
        .apply("Read sensor groups", KafkaIO.<Event, String>read()
            .withBootstrapServers(bootstrapServer)
            .withTopic(configurationTopic)
            .withKeyDeserializer(EventDeserializer.class)
            .withValueDeserializer(StringDeserializer.class)
            .withConsumerConfigUpdates(consumerConfigConfiguration)
            .withoutMetadata())
        // Only forward relevant changes in the hierarchie
        .apply("Filter changed and status events",
            Filter.by(new SerializableFunction<KV<Event, String>, Boolean>() {
              @Override
              public Boolean apply(final KV<Event, String> kv) {
                return kv.getKey() == Event.SENSOR_REGISTRY_CHANGED
                    || kv.getKey() == Event.SENSOR_REGISTRY_STATUS;
              }
            }))
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
            ParDo.of(
                new DoFn<KV<String, ActivePowerRecord>, KV<SensorParentKey, ActivePowerRecord>>() {
                  @StateId("parents")
                  private final StateSpec<ValueState<Set<String>>> parents = StateSpecs.value();

                  // Generate a KV-pair for every child-parent match
                  @ProcessElement
                  public void processElement(@Element final KV<String, ActivePowerRecord> kv,
                      final OutputReceiver<KV<SensorParentKey, ActivePowerRecord>> out,
                      @StateId("parents") final ValueState<Set<String>> state,
                      final ProcessContext c) {
                    final ActivePowerRecord record = kv.getValue() == null ? null : kv.getValue();
                    final Set<String> newParents =
                        c.sideInput(childParentPairMap).get(kv.getKey()) == null
                            ? Collections.emptySet()
                            : c.sideInput(childParentPairMap).get(kv.getKey());
                    final Set<String> oldParents =
                        MoreObjects.firstNonNull(state.read(), Collections.emptySet());
                    // Forward new Pairs if they exist
                    if (!newParents.isEmpty()) {
                      for (final String parent : newParents) {

                        // Forward flat mapped record
                        final SensorParentKey key = new SensorParentKey(kv.getKey(), parent);
                        out.output(KV.of(key, record));
                      }
                    }
                    if (!newParents.equals(oldParents)) {
                      for (final String oldParent : oldParents) {
                        if (!newParents.contains(oldParent)) {
                          // Forward Delete
                          final SensorParentKey key = new SensorParentKey(kv.getKey(), oldParent);
                          out.output(KV.of(key, null));
                        }
                      }
                      state.write(newParents);
                    }
                  }
                }).withSideInputs(childParentPairMap))

            .apply("Filter only latest changes", Latest.perKey())
            .apply("Filter out null values",
                Filter.by(
                    new SerializableFunction<KV<SensorParentKey, ActivePowerRecord>, Boolean>() {
                      @Override
                      public Boolean apply(final KV<SensorParentKey, ActivePowerRecord> kv) {
                        return kv.getValue() != null;
                      }
                    }));
    // Aggregate for every sensor group of the current level
    final PCollection<KV<String, AggregatedActivePowerRecord>>
        aggregations = flatMappedValues
        .apply("Set key to group", MapElements.via(
            new SimpleFunction<KV<SensorParentKey,
                ActivePowerRecord>, KV<String, ActivePowerRecord>>() {
              @Override
              public KV<String, ActivePowerRecord> apply(
                  final KV<SensorParentKey, ActivePowerRecord> kv) {

                return KV.of(kv.getKey().getParent(), kv.getValue());
              }
            }))
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
            MapElements.via(
              new SimpleFunction<KV<String, AggregatedActivePowerRecord>,
                  KV<String, AggregatedActivePowerRecord>>() {
                @Override
                public KV<String, AggregatedActivePowerRecord> apply(
                    final KV<String, AggregatedActivePowerRecord> kv) {
                  final AggregatedActivePowerRecord record = new AggregatedActivePowerRecord(
                      kv.getKey(), kv.getValue().getTimestamp(), kv.getValue().getCount(),
                      kv.getValue().getSumInW(), kv.getValue().getAverageInW());
                  return KV.of(kv.getKey(), record);
                }
              }));



    aggregations.apply("Write to aggregation results",
        KafkaIO.<String, AggregatedActivePowerRecord>write()
            .withBootstrapServers(bootstrapServer)
            .withTopic(outputTopic)
            .withKeySerializer(StringSerializer.class)
            .withValueSerializer(AggregatedActivePowerRecordSerializer.class));


    aggregations
        .apply("Write to feedback topic", KafkaIO.<String, AggregatedActivePowerRecord>write()
            .withBootstrapServers(bootstrapServer)
            .withTopic(feedbackTopic)
            .withKeySerializer(StringSerializer.class)
            .withValueSerializer(AggregatedActivePowerRecordSerializer.class));


    pipeline.run().waitUntilFinish();
  }

}
