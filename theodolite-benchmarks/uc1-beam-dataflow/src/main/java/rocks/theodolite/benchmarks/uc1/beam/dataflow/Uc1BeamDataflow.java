package rocks.theodolite.benchmarks.uc1.beam.dataflow;

import org.apache.beam.runners.dataflow.DataflowRunner;
import rocks.theodolite.benchmarks.commons.beam.BeamService;
import rocks.theodolite.benchmarks.uc1.beam.PipelineFactory;

/**
 * Implementation of the use case Database Storage using Apache Beam with the Google Cloud Dataflow
 * runner.
 */
public final class Uc1BeamDataflow {

  private Uc1BeamDataflow() {}

  /**
   * Main method.
   */
  public static void main(final String[] args) {
    new BeamService(PipelineFactory.factory(), DataflowRunner.class, args).runStandalone();
  }
}
