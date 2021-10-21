package application;

import com.google.gson.Gson;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.HashMap;
import org.apache.beam.runners.samza.SamzaRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Implementation of the use case Database Storage using Apache Beam with the Samza Runner. To
 * execute locally in standalone start Kafka, Zookeeper, the schema-registry and the workload
 * generator using the delayed_startup.sh script. Add
 * --configFactory=org.apache.samza.config.factories.PropertiesConfigFactory
 * --configFilePath=${workspace_loc:uc1-application-samza}/config/standalone_local.properties
 * --samzaExecutionEnvironment=STANDALONE --maxSourceParallelism=1024 --as program arguments. To
 * persist logs add ${workspace_loc:/uc4-application-samza/eclipseConsoleLogs.log} as Output File
 * under Standard Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc1ApplicationBeam {
  private static final Logger LOGGER = LoggerFactory.getLogger(Uc1ApplicationBeam.class);
  private static final String BOOTSTRAP = "KAFKA_BOOTSTRAP_SERVERS";
  private static final String INPUT = "INPUT";
  private static final String SCHEMA_REGISTRY = "SCHEMA_REGISTRY_URL";
  private static final String YES = "true";
  private static final String USE_AVRO_READER = YES;
  private static final String AUTO_COMMIT_CONFIG = YES;



  /**
  * Private constructor to avoid instantiation.
  */
  private Uc1ApplicationBeam() {
    throw new UnsupportedOperationException();
  }

  /**
  * Main method.
  *
  */
  @SuppressWarnings({"unchecked", "rawtypes","unused"})
  public static void main(final String[] args) {

    // Set Configuration for Kafka
    final String bootstrapServer =
        System.getenv(BOOTSTRAP) == null ? "my-confluent-cp-kafka:9092"
            : System.getenv(BOOTSTRAP);
    final String inputTopic = System.getenv(INPUT) == null ? "input" : System.getenv(INPUT);
    final String schemaRegistryUrl =
        System.getenv(SCHEMA_REGISTRY) == null ? "http://my-confluent-cp-schema-registry:8081"
            : System.getenv(SCHEMA_REGISTRY);
    // Set consumer configuration for the schema registry and commits back to Kafka
    final HashMap<String, Object> consumerConfig = new HashMap<>();
    consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, AUTO_COMMIT_CONFIG);
    consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerConfig.put("schema.registry.url", schemaRegistryUrl);
    consumerConfig.put("specific.avro.reader", USE_AVRO_READER);
    consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "uc-application");

    // Create Pipeline Options from args. Current Execution Parameters for local execution are:
    // --configFactory=org.apache.samza.config.factories.PropertiesConfigFactory
    // --configFilePath=${workspace_loc:uc1-application-samza}/config/standalone_local.properties
    // --samzaExecutionEnvironment=STANDALONE
    // --maxSourceParallelism=1024

    final LoggKeys logging = new LoggKeys();

    final PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
    options.setJobName("ucapplication");
    options.setRunner(SamzaRunner.class);

    final Pipeline pipeline = Pipeline.create(options);

    final CoderRegistry cr = pipeline.getCoderRegistry();

    // Set Coders for Classes that will be distributed
    cr.registerCoderForClass(ActivePowerRecord.class, AvroCoder.of(ActivePowerRecord.SCHEMA$));

    final PTransform<PBegin, PCollection<KV<String, ActivePowerRecord>>> kafka =
        KafkaIO.<String, ActivePowerRecord>read()
            .withBootstrapServers(bootstrapServer)
            .withTopic(inputTopic)
            .withKeyDeserializer(StringDeserializer.class)
            .withValueDeserializerAndCoder((Class) KafkaAvroDeserializer.class,
                AvroCoder.of(ActivePowerRecord.class))
            .withConsumerConfigUpdates(consumerConfig)
            .withoutMetadata();

    // Apply pipeline transformations
    // Read from Kafka
    pipeline.apply(kafka)
        // Map to Gson
        .apply(MapElements
            .via(
                new SimpleFunction<KV<String, ActivePowerRecord>, KV<String, String>>() {
                  private transient Gson gsonObj = new Gson();

                  @Override
                  public KV<String, String> apply(
                      final KV<String, ActivePowerRecord> kv) {
                    if (this.gsonObj == null) {
                      this.gsonObj = new Gson();
                    }
                    final String gson = this.gsonObj.toJson(kv.getValue());
                    return KV.of(kv.getKey(), gson);
                  }
                }))
        // Print to console
        .apply(ParDo.of(logging));
    // Start execution
    pipeline.run().waitUntilFinish();
  }

  /**
   * Logs all Keys it reads.
   */
  @SuppressWarnings({"unused"})
  private static class LoggKeys extends DoFn<KV<String, String>,KV<String, String>> {
    private static final long serialVersionUID = 4328743;

    @ProcessElement
    public void processElement(@Element final KV<String, String> kv,
                               final OutputReceiver<KV<String, String>> out) {
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Key: " + kv.getKey() + "Value: " + kv.getValue());
      }
    }
  }

}



