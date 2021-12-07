package application;

import org.apache.beam.runners.flink.FlinkRunner;
import org.apache.beam.sdk.Pipeline;
import theodolite.commons.beam.AbstractBeamService;

/**
 * Implementation of the use case Hierarchical Aggregation using Apache Beam with the Flink
 * Runner.
 **/
public final class Uc4BeamFlink extends AbstractBeamService {


  /**
   * Private constructor setting specific options for this use case.
   */
  private Uc4BeamFlink(final String[] args) { //NOPMD
    super(args);
    this.options.setRunner(FlinkRunner.class);
  }

  /**
   * Start running this microservice.
   */
  public static void main(final String[] args) {

    final Uc4BeamFlink uc4BeamFlink = new Uc4BeamFlink(args);

    final Pipeline pipeline = new Uc4BeamPipeline(uc4BeamFlink.options, uc4BeamFlink.getConfig());

    pipeline.run().waitUntilFinish();
  }

}
