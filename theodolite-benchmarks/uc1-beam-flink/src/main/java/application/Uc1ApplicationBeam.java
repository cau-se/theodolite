package application;

import com.google.gson.Gson;
import java.util.Properties;
import org.apache.beam.runners.flink.FlinkRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theodolite.commons.beam.AbstractBeamService;
import theodolite.commons.beam.ConfigurationKeys;
import theodolite.commons.beam.kafka.KafkaAggregatedPowerRecordReader;
import titan.ccp.model.records.ActivePowerRecord;


/**
 * Implementation of the use case Database Storage using Apache Beam with the Flink Runner. To
 * execute locally in standalone start Kafka, Zookeeper, the schema-registry and the workload
 * generator using the delayed_startup.sh script. Start a Flink cluster and pass its REST adress
 * using--flinkMaster as run parameter. To persist logs add
 * ${workspace_loc:/uc1-application-samza/eclipseConsoleLogs.log} as Output File under Standard
 * Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc1ApplicationBeam extends AbstractBeamService {

  private static final Logger LOGGER = LoggerFactory.getLogger(Uc1ApplicationBeam.class);
  private final String inputTopic = CONFIG.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC);
  private final String bootstrapServer =
      CONFIG.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);

  /**
   * Private constructor setting specific options for this use case.
   */
  private Uc1ApplicationBeam(final String[] args) { //NOPMD
    super(args);
    LOGGER.info(this.options.toString());
    this.options.setRunner(FlinkRunner.class);
  }

  /**
   * Main method.
   */
  @SuppressWarnings({"unchecked", "rawtypes", "unused"})
  public static void main(final String[] args) {

    final Uc1ApplicationBeam uc1 = new Uc1ApplicationBeam(args);

    // create pipeline
    final Pipeline pipeline = Pipeline.create(uc1.options);

    // Set Coders for Classes that will be distributed
    final CoderRegistry cr = pipeline.getCoderRegistry();
    cr.registerCoderForClass(ActivePowerRecord.class, AvroCoder.of(ActivePowerRecord.SCHEMA$));

    // build KafkaConsumerConfig
    final Properties consumerConfig = uc1.buildConsumerConfig();

    // Create Pipeline transformations
    final PTransform<PBegin, PCollection<KV<String, ActivePowerRecord>>> kafka =
        new KafkaAggregatedPowerRecordReader(uc1.bootstrapServer, uc1.inputTopic, consumerConfig);

    final LogKeyValue logKeyValue = new LogKeyValue();

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
        .apply(ParDo.of(logKeyValue));
    // Submit job and start execution
    pipeline.run().waitUntilFinish();
  }


  /**
   * Logs all Key Value pairs.
   */
  @SuppressWarnings({"unused"})
  private static class LogKeyValue extends DoFn<KV<String, String>, KV<String, String>> {
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

