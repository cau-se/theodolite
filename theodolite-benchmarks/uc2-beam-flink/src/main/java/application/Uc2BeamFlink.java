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

    final Uc2BeamFlink uc2BeamFlink = new Uc2BeamFlink(args);

    final Pipeline pipeline = new Uc2BeamPipeline(uc2BeamFlink.options, uc2BeamFlink.getConfig());

    pipeline.run().waitUntilFinish();
  }
}

