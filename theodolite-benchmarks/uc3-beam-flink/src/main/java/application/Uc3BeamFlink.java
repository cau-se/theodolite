package application;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.HashMap;
import java.util.Map;
import org.apache.beam.runners.flink.FlinkRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.Duration;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Implementation of the use case Aggregation based on Time Attributes using Apache Beam with the
 * Flink Runner. To run locally in standalone start Kafka, Zookeeper, the schema-registry and the
 * workload generator using the delayed_startup.sh script. And configure the Kafka, Zookeeper and
 * Schema Registry urls accordingly. Start a Flink cluster and pass its REST adress
 * using--flinkMaster as run parameter. To persist logs add
 * ${workspace_loc:/uc4-application-samza/eclipseConsoleLogs.log} as Output File under Standard
 * Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc3ApplicationBeam {

  private static final String JOB_NAME = "Uc3Application";
  private static final String BOOTSTRAP = "KAFKA_BOOTSTRAP_SERVERS";
  private static final String INPUT = "INPUT";
  private static final String OUTPUT = "OUTPUT";
  private static final String SCHEMA_REGISTRY = "SCHEMA_REGISTRY_URL";
  private static final String YES = "true";
  private static final String USE_AVRO_READER = YES;
  private static final String AUTO_COMMIT_CONFIG = YES;
  private static final String KAFKA_WINDOW_DURATION_DAYS  = "KAFKA_WINDOW_DURATION_MINUTES";
  private static final String AGGREGATION_ADVANCE_DAYS  = "AGGREGATION_ADVANCE_DAYS";
  private static final String TRIGGER_INTERVAL  = "TRIGGER_INTERVAL";

  /**
   * Private constructor to avoid instantiation.
   */
  private Uc3ApplicationBeam() {
    throw new UnsupportedOperationException();
  }

  /**
   * Start running this microservice.
   */
  public static void main(final String[] args) {

    // Set Configuration for Windows
    final int windowDuration = Integer.parseInt(
        System.getenv(KAFKA_WINDOW_DURATION_DAYS) == null
            ? "30" : System.getenv(KAFKA_WINDOW_DURATION_DAYS));
    final Duration duration = Duration.standardDays(windowDuration);

    final int aggregationAdvance = Integer.parseInt(
        System.getenv(AGGREGATION_ADVANCE_DAYS) == null
            ? "1" : System.getenv(AGGREGATION_ADVANCE_DAYS));
    final Duration advance = Duration.standardDays(aggregationAdvance);

    final int triggerInterval = Integer.parseInt(
        System.getenv(TRIGGER_INTERVAL) == null
            ? "15" : System.getenv(TRIGGER_INTERVAL));

    final Duration triggerDelay = Duration.standardSeconds(triggerInterval);

    // Set Configuration for Kafka
    final String bootstrapServer =
        System.getenv(BOOTSTRAP) == null ? "my-confluent-cp-kafka:9092"
            : System.getenv(BOOTSTRAP);
    final String inputTopic = System.getenv(INPUT) == null ? "input" : System.getenv(INPUT);
    final String outputTopic = System.getenv(OUTPUT) == null ? "output" : System.getenv(OUTPUT);
    final String schemaRegistryUrl =
        System.getenv(SCHEMA_REGISTRY) == null ? "http://my-confluent-cp-schema-registry:8081"
            : System.getenv(SCHEMA_REGISTRY);

    final Map<String, Object> consumerConfig = buildConsumerConfig(schemaRegistryUrl);
    final StatsKeyFactory<HourOfDayKey> keyFactory = new HourOfDayKeyFactory();

    final PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
    options.setRunner(FlinkRunner.class);
    options.setJobName(JOB_NAME);
    final Pipeline pipeline = Pipeline.create(options);
    final CoderRegistry cr = pipeline.getCoderRegistry();
    registerCoders(cr);

    @SuppressWarnings({"rawtypes", "unchecked"})
    final PTransform<PBegin, PCollection<KV<String, ActivePowerRecord>>> kafka =
        KafkaIO.<String, ActivePowerRecord>read()
            .withBootstrapServers(bootstrapServer)
            .withTopic(inputTopic)
            .withKeyDeserializer(StringDeserializer.class)
            .withValueDeserializerAndCoder((Class) KafkaAvroDeserializer.class,
                AvroCoder.of(ActivePowerRecord.class))
            .withConsumerConfigUpdates(consumerConfig)
            // Set TimeStampPolicy for event time
            .withTimestampPolicyFactory(
                (tp, previousWaterMark) -> new EventTimePolicy(previousWaterMark))
            .withoutMetadata();
    // Apply pipeline transformations
    // Read from Kafka
    pipeline.apply(kafka)
        // Map to correct time format
        .apply(MapElements.via(new MapTimeFormat()))

        // Apply a sliding window
        .apply(Window
            .<KV<HourOfDayKey, ActivePowerRecord>>into(SlidingWindows.of(duration).every(advance))
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
            .via(new SimpleFunction<KV<HourOfDayKey, Stats>, KV<String, String>>() {
              @Override
              public KV<String, String> apply(final KV<HourOfDayKey, Stats> kv) {
                return KV.of(keyFactory.getSensorId(kv.getKey()), kv.getValue().toString());
              }
            }))
        // Write to Kafka
        .apply(KafkaIO.<String, String>write()
            .withBootstrapServers(bootstrapServer)
            .withTopic(outputTopic)
            .withKeySerializer(StringSerializer.class)
            .withValueSerializer(StringSerializer.class));

    pipeline.run().waitUntilFinish();
  }

  /**
   * Builds a configuration for a Kafka consumer.
   * @param schemaRegistryUrl the url to the SchemaRegistry.
   * @return the configuration.
   */
  public static Map<String, Object> buildConsumerConfig(final String schemaRegistryUrl) {

    // Set consumer configuration for the schema registry and commits back to Kafka
    final HashMap<String, Object> consumerConfig = new HashMap<>();
    consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, AUTO_COMMIT_CONFIG);
    consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerConfig.put("schema.registry.url", schemaRegistryUrl);
    consumerConfig.put("specific.avro.reader", USE_AVRO_READER);
    consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, JOB_NAME);

    return consumerConfig;
  }

  /**
   * Registers all Coders for all needed Coders.
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

