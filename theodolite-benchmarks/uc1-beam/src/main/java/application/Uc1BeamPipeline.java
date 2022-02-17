package application;

import java.util.Map;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Values;
import org.apache.commons.configuration2.Configuration;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseAdapter;
import rocks.theodolite.benchmarks.uc1.commons.logger.LogWriterFactory;
import theodolite.commons.beam.AbstractPipeline;
import theodolite.commons.beam.kafka.KafkaActivePowerTimestampReader;
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

  private final DatabaseAdapter<String> databaseAdapter = LogWriterFactory.forJson();

  protected Uc1BeamPipeline(final PipelineOptions options, final Configuration config) {
    super(options, config);

    // Set Coders for Classes that will be distributed
    final CoderRegistry cr = this.getCoderRegistry();
    cr.registerCoderForClass(ActivePowerRecord.class, AvroCoder.of(ActivePowerRecord.SCHEMA$));

    // build KafkaConsumerConfig
    final Map<String, Object> consumerConfig = this.buildConsumerConfig();

    // Create Pipeline transformations
    final KafkaActivePowerTimestampReader kafka =
        new KafkaActivePowerTimestampReader(this.bootstrapServer, this.inputTopic, consumerConfig);

    // Apply pipeline transformations
    // Read from Kafka
    this.apply(kafka)
        .apply(Values.create())
        .apply(MapElements.via(new ConverterAdapter<>(
            this.databaseAdapter.getRecordConverter(),
            String.class)))
        .apply(ParDo.of(new WriterAdapter<>(this.databaseAdapter.getDatabaseWriter())));
  }
}

