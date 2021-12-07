package application;

import java.util.Map;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.configuration2.Configuration;
import theodolite.commons.beam.AbstractPipeline;
import theodolite.commons.beam.kafka.KafkaActivePowerRecordReader;
import titan.ccp.model.records.ActivePowerRecord;


/**
 * Implementation of the use case Database Storage using Apache Beam with the Flink Runner. To
 * execute locally in standalone start Kafka, Zookeeper, the schema-registry and the workload
 * generator using the delayed_startup.sh script. Start a Flink cluster and pass its REST adress
 * using--flinkMaster as run parameter. To persist logs add
 * ${workspace_loc:/uc1-application-samza/eclipseConsoleLogs.log} as Output File under Standard
 * Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc1BeamPipeline extends AbstractPipeline {

  protected Uc1BeamPipeline(final PipelineOptions options, final Configuration config) {
    super(options, config);

    // Set Coders for Classes that will be distributed
    final CoderRegistry cr = this.getCoderRegistry();
    cr.registerCoderForClass(ActivePowerRecord.class, AvroCoder.of(ActivePowerRecord.SCHEMA$));

    // build KafkaConsumerConfig
    final Map<String, Object> consumerConfig = buildConsumerConfig();

    // Create Pipeline transformations
    final PTransform<PBegin, PCollection<KV<String, ActivePowerRecord>>> kafka =
        new KafkaActivePowerRecordReader(bootstrapServer, inputTopic, consumerConfig);

    final LogKeyValue logKeyValue = new LogKeyValue();
    final MapToGson mapToGson = new MapToGson();

    // Apply pipeline transformations
    // Read from Kafka
    this.apply(kafka)
        // Map to Gson
        .apply(MapElements
            .via(mapToGson))
        // Print to console
        .apply(ParDo.of(logKeyValue));
  }
}

