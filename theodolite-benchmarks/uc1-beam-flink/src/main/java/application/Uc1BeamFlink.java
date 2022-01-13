package application;

import org.apache.beam.runners.flink.FlinkRunner;
import theodolite.commons.beam.AbstractBeamService;

/**
 * Implementation of the use case Database Storage using Apache Beam with the Flink Runner. To
 * execute locally in standalone start Kafka, Zookeeper, the schema-registry and the workload
 * generator using the delayed_startup.sh script. Start a Flink cluster and pass its REST adress
 * using--flinkMaster as run parameter. To persist logs add
 * ${workspace_loc:/uc1-application-samza/eclipseConsoleLogs.log} as Output File under Standard
 * Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc1BeamFlink extends AbstractBeamService {

  /**
   * Private constructor setting specific options for this use case.
   */
  private Uc1BeamFlink(final String[] args) { //NOPMD
    super(args);
    this.options.setRunner(FlinkRunner.class);
  }

  /**
   * Main method.
   */
  public static void main(final String[] args) {

    // Create application via configurations
    final Uc1BeamFlink uc1 = new Uc1BeamFlink(args);

    // Create pipeline with configurations
    final Uc1BeamPipeline pipeline = new Uc1BeamPipeline(uc1.options, uc1.getConfig());

    // Submit job and start execution
    pipeline.run().waitUntilFinish();
  }

}

