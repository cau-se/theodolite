package application;

import org.apache.beam.runners.flink.FlinkRunner;
import theodolite.commons.beam.BeamService;

/**
 * Implementation of the use case Hierarchical Aggregation using Apache Beam with the Flink Runner.
 **/
public final class Uc4BeamFlink {

  private Uc4BeamFlink() {}

  /**
   * Start running this microservice.
   */
  public static void main(final String[] args) {
    new BeamService(PipelineFactory.factory(), FlinkRunner.class, args).run();
  }

}
