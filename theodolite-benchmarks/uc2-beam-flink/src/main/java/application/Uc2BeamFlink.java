package application;

import org.apache.beam.runners.flink.FlinkRunner;
import org.apache.beam.sdk.Pipeline;
import theodolite.commons.beam.AbstractBeamService;

/**
 * Implementation of the use case Downsampling using Apache Beam with the Flink Runner. To execute
 * locally in standalone start Kafka, Zookeeper, the schema-registry and the workload generator
 * using the delayed_startup.sh script. Start a Flink cluster and pass its REST adress
 * using--flinkMaster as run parameter.
 */
public final class Uc2BeamFlink extends AbstractBeamService {
  private static final String JOB_NAME = "Uc2Application";
  private static final String BOOTSTRAP = "KAFKA_BOOTSTRAP_SERVERS";
  private static final String INPUT = "INPUT";
  private static final String OUTPUT = "OUTPUT";
  private static final String SCHEMA_REGISTRY = "SCHEMA_REGISTRY_URL";
  private static final String YES = "true";
  private static final String USE_AVRO_READER = YES;
  private static final String AUTO_COMMIT_CONFIG = YES;
  private static final String KAFKA_WINDOW_DURATION_MINUTES = "KAFKA_WINDOW_DURATION_MINUTES";

  /**
   * Private constructor setting specific options for this use case.
   */
  private Uc2BeamFlink(final String[] args) { //NOPMD
    super(args);
    this.options.setRunner(FlinkRunner.class);
  }

  /**
   * Start running this microservice.
   */
  @SuppressWarnings({"serial", "unchecked", "rawtypes"})
  public static void main(final String[] args) {

    Uc2BeamFlink uc2BeamFlink = new Uc2BeamFlink(args);

    Pipeline pipeline = new Uc2BeamPipeline(uc2BeamFlink.options, uc2BeamFlink.getConfig());

    pipeline.run().waitUntilFinish();
  }
}

