package application;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.Values;
import org.apache.commons.configuration2.Configuration;
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

  public static final String SINK_TYPE_KEY = "sink.type";

  protected Uc1BeamPipeline(final PipelineOptions options, final Configuration config) {
    super(options, config);

    final SinkType sinkType = SinkType.from(config.getString(SINK_TYPE_KEY));

    // Set Coders for Classes that will be distributed
    final CoderRegistry cr = this.getCoderRegistry();
    cr.registerCoderForClass(ActivePowerRecord.class, AvroCoder.of(ActivePowerRecord.SCHEMA$));

    // Create Pipeline transformations
    final KafkaActivePowerTimestampReader kafka = new KafkaActivePowerTimestampReader(
        this.bootstrapServer,
        this.inputTopic,
        this.buildConsumerConfig());

    // Apply pipeline transformations
    // Read from Kafka
    this.apply(kafka)
        .apply(Values.create())
        .apply(sinkType.create(config));
  }

}

