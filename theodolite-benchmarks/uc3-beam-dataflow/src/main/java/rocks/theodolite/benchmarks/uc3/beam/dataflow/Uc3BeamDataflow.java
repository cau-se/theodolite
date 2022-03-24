package rocks.theodolite.benchmarks.uc3.beam.dataflow;

import org.apache.beam.runners.dataflow.DataflowRunner;
import rocks.theodolite.benchmarks.commons.beam.BeamService;
import rocks.theodolite.benchmarks.uc3.beam.PipelineFactory;

/**
 * Implementation of the use case Aggregation based on Time Attributes using Apache Beam with the
 * Google Cloud Dataflow runner.
 */
public final class Uc3BeamDataflow {

  private Uc3BeamDataflow() {}

  /**
   * Start running this microservice.
   */
  public static void main(final String[] args) {
    new BeamService(PipelineFactory.factory(), DataflowRunner.class, args).runStandalone();
  }

}

