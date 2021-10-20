package application;

import com.google.gson.Gson;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.HashMap;
import org.apache.beam.runners.flink.FlinkRunner;
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
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Implementation of the use case Database Storage using Apache Beam with the Flink Runner. To
 * execute locally in standalone start Kafka, Zookeeper, the schema-registry and the workload
 * generator using the delayed_startup.sh script. Start a Flink cluster and pass its REST adress
 * using--flinkMaster as run parameter. To persist logs add
 * ${workspace_loc:/uc1-application-samza/eclipseConsoleLogs.log} as Output File under Standard
 * Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public class Uc1ApplicationBeam {
  @SuppressWarnings({"unchecked", "rawtypes", "serial"})
  public static void main(final String[] args) {
    // Set Configuration for Kafka
    final String bootstrapServer =
        System.getenv("KAFKA_BOOTSTRAP_SERVERS") != null ? System.getenv("KAFKA_BOOTSTRAP_SERVERS")
            : "my-confluent-cp-kafka:9092";
    final String inputTopic = System.getenv("INPUT") != null ? System.getenv("INPUT") : "input";
    final String schemaRegistryURL =
        System.getenv("SCHEMA_REGISTRY_URL") != null ? System.getenv("SCHEMA_REGISTRY_URL")
            : "http://my-confluent-cp-schema-registry:8081";
    // Set consumer configuration for the schema registry and commits back to Kafka
    final HashMap<String, Object> consumerConfig = new HashMap<>();
    consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerConfig.put("schema.registry.url", schemaRegistryURL);
    consumerConfig.put("specific.avro.reader", "true");
    consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "uc-application");


    final PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
    options.setJobName("ucapplication");
    options.setRunner(FlinkRunner.class);

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
                  transient Gson gsonObj = new Gson();

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
        .apply(ParDo.of(new DoFn<KV<String, String>, KV<String, String>>() {
          @ProcessElement
          public void processElement(@Element final KV<String, String> kv,
              final OutputReceiver<KV<String, String>> out) {
            System.out.println("Key: " + kv.getKey() + "Value: " + kv.getValue());
//            out.output(kv);
          }
        }));

    // Submit job and start execution
    pipeline.run().waitUntilFinish();

  }
}

