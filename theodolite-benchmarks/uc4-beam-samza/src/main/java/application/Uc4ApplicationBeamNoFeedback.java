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
import org.apache.beam.sdk.transforms.Latest;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Repeatedly;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.Duration;
import serialization.AggregatedActivePowerRecordCoder;
import serialization.AggregatedActivePowerRecordSerializer;
import serialization.EventCoder;
import serialization.EventDeserializer;
import serialization.SensorParentKeyCoder;
import theodolite.commons.beam.kafka.EventTimePolicy;
import titan.ccp.configuration.events.Event;
import titan.ccp.model.records.ActivePowerRecord;
import titan.ccp.model.records.AggregatedActivePowerRecord;

/**
 * Usecase implementation without the feedback.
 */
public final class Uc4ApplicationBeamNoFeedback {

  private static final String JOB_NAME = "Uc4Application";
  private static final String YES = "true";
  private static final String USE_AVRO_READER = YES;
  private static final String AUTO_COMMIT_CONFIG = YES;

  private static final String AUTO_OFFSET_RESET_CONFIG  = "earliest";
  private static final int DELAY  = 5;

  private static final String PARENTS = "Parents: ";
  private static final String VALUE_KEY = "ValueKey: ";
  private static final String VALUE_IN_W = "ValueInW: ";
  private static final String TIMESTAMP = "Timestamp: ";

  /**
   * Private constructor to avoid instantiation.
   */
  private Uc4ApplicationBeamNoFeedback() {
    throw new UnsupportedOperationException();
  }

  /**
   * Start executing this microservice.
   */
  @SuppressWarnings({"serial", "unchecked", "rawtypes"})
  public static void main(final String[] args) {

    final String inputTopic = "input";
    final String outputTopic = "output";
    final String bootstrapServer = "localhost:9092";
    final String configurationTopic = "configuration";
    final String schemaRegistryUrl = "http://localhost:8081";
    final Duration duration = Duration.standardSeconds(15);

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



    // final DirectOptions options =
    // PipelineOptionsFactory.fromArgs(args).create().as(DirectOptions.class);
    // options.setRunner(DirectRunner.class);
    // options.setJobName("ucapplication");
    // options.setTargetParallelism(1);
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
    // SensorRegistry
    cr.registerCoderForClass(SensorParentKey.class, new SensorParentKeyCoder());
    cr.registerCoderForClass(StatsAccumulator.class, AvroCoder.of(StatsAccumulator.class));


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

            // .commitOffsetsInFinalize()
            .withoutMetadata();
    // Apply pipeline transformations
    // Read from Kafka
    final PCollection<KV<String, ActivePowerRecord>> values = pipeline.apply(kafka)
        .apply("Apply Winddows", Window.into(FixedWindows.of(duration)));


    // Build the configuration stream from a changelog.
    final PCollection<KV<String, Set<String>>> configurationStream = pipeline
        .apply("Read sensor groups", KafkaIO.<Event, String>read()
            .withBootstrapServers(bootstrapServer)
            .withTopic(configurationTopic)
            .withKeyDeserializer(EventDeserializer.class)
            .withValueDeserializer(StringDeserializer.class)
            .withConsumerConfigUpdates(consumerConfigConfiguration)
            // .commitOffsetsInFinalize()
            .withoutMetadata())

        .apply("Generate Parents for every Sensor", ParDo.of(new GenerateParentsFn()))
        .apply("Update child and parent pairs", ParDo.of(new UpdateChildParentPairs()))
        .apply("Set trigger for configurations", Window
            .<KV<String, Set<String>>>configure()
            .triggering(Repeatedly.forever(
                AfterProcessingTime.pastFirstElementInPane()
                    .plusDelayOf(Duration.standardSeconds(DELAY))))
            .accumulatingFiredPanes());
    // This may need to be changed to eliminate duplicates in first iteration


    final PCollectionView<Map<String, Set<String>>> childParentPairMap =
        // configurationStream.apply(Latest.perKey())
        configurationStream.apply(View.asMap());


    final PCollection<KV<SensorParentKey, ActivePowerRecord>> flatMappedValues =
        values.apply(
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
                    System.out.println("Map Entry for Key: " + newParents.toString());
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

            .apply("Debugging output before filtering latest", ParDo.of(
                new DoFn<KV<SensorParentKey, ActivePowerRecord>,
                    KV<SensorParentKey, ActivePowerRecord>>() {
                  @ProcessElement
                  public void processElement(
                      @Element final KV<SensorParentKey, ActivePowerRecord> kv,
                      final OutputReceiver<KV<SensorParentKey, ActivePowerRecord>> out,
                      final ProcessContext c) {
                    System.out.println("Before filter latest Sensor: " + kv.getKey().getSensor()
                        + PARENTS + kv.getKey().getParent() + VALUE_KEY
                        + kv.getValue().getIdentifier() + VALUE_IN_W
                        + kv.getValue().getValueInW()
                        + TIMESTAMP + kv.getValue().getTimestamp());
                    out.output(kv);
                  }
                }))
            .apply("Filter only latest changes", Latest.perKey())
            .apply("Debugging output after filtering latest", ParDo.of(
                new DoFn<KV<SensorParentKey, ActivePowerRecord>,
                    KV<SensorParentKey, ActivePowerRecord>>() {
                  @ProcessElement
                  public void processElement(
                      @Element final KV<SensorParentKey, ActivePowerRecord> kv,
                      final OutputReceiver<KV<SensorParentKey, ActivePowerRecord>> out,
                      final ProcessContext c) {
                    System.out.println("After filter latest Sensor: " + kv.getKey().getSensor()
                        + PARENTS + kv.getKey().getParent() + VALUE_KEY
                        + kv.getValue().getIdentifier() + VALUE_IN_W
                        + kv.getValue().getValueInW()
                        + TIMESTAMP + kv.getValue().getTimestamp());
                    out.output(kv);
                  }
                }));


    final PCollection<KV<String, AggregatedActivePowerRecord>> aggregations = flatMappedValues

        .apply("Set key to group", MapElements.via(
            new SimpleFunction<KV<SensorParentKey, ActivePowerRecord>,
                KV<String, ActivePowerRecord>>() {
              @Override
              public KV<String, ActivePowerRecord> apply(
                  final KV<SensorParentKey, ActivePowerRecord> kv) {
                System.out.println("key set to group" + kv.getKey() + TIMESTAMP
                    + kv.getValue().getTimestamp());
                return KV.of(kv.getKey().getParent(), kv.getValue());
              }
            }))
        .apply("Aggregate per group", Combine.perKey(new RecordAggregation()))
        .apply("Set the Identifier in AggregatedActivePowerRecord", MapElements.via(
            new SimpleFunction<KV<String, AggregatedActivePowerRecord>,
                KV<String, AggregatedActivePowerRecord>>() {
              @Override
              public KV<String, AggregatedActivePowerRecord> apply(
                  final KV<String, AggregatedActivePowerRecord> kv) {
                final AggregatedActivePowerRecord record = new AggregatedActivePowerRecord(
                    kv.getKey(), kv.getValue().getTimestamp(), kv.getValue().getCount(),
                    kv.getValue().getSumInW(), kv.getValue().getAverageInW());
                System.out.println("set identifier to: " + record.getIdentifier() + TIMESTAMP
                    + record.getTimestamp());
                return KV.of(kv.getKey(), record);
              }
            }));


    aggregations.apply("Print Stats", MapElements.via(
        new SimpleFunction<KV<String, AggregatedActivePowerRecord>,
            KV<String, AggregatedActivePowerRecord>>() {

          @Override
          public KV<String, AggregatedActivePowerRecord> apply(
              final KV<String, AggregatedActivePowerRecord> kv) {
            System.out.println("Output: Key: "
                + kv.getKey()
                + " Identifier: " + kv.getValue().getIdentifier()
                + TIMESTAMP + kv.getValue().getTimestamp()
                + " Avg: " + kv.getValue().getAverageInW()
                + " Count: " + kv.getValue().getCount()
                + " Sum: " + kv.getValue().getSumInW());
            //
            return kv;
          }
        }))
        .apply("Write to aggregation results", KafkaIO.<String, AggregatedActivePowerRecord>write()
            .withBootstrapServers(bootstrapServer)
            .withTopic(outputTopic)
            .withKeySerializer(StringSerializer.class)
            .withValueSerializer(AggregatedActivePowerRecordSerializer.class));

    pipeline.run().waitUntilFinish();
  }

}
