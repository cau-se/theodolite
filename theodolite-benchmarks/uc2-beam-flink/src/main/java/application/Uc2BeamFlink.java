package application;

import org.apache.beam.runners.flink.FlinkRunner;
import theodolite.commons.beam.BeamService;

/**
 * Implementation of the use case Downsampling using Apache Beam with the Flink Runner. To execute
 * locally in standalone start Kafka, Zookeeper, the schema-registry and the workload generator
 * using the delayed_startup.sh script. Start a Flink cluster and pass its REST adress
 * using--flinkMaster as run parameter.
 */
public final class Uc2BeamFlink {

  private Uc2BeamFlink() {}

  public static void main(final String[] args) {
    new BeamService(PipelineFactory.factory(), FlinkRunner.class, args).run();
  }
}

