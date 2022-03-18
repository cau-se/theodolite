package rocks.theodolite.benchmarks.uc3.beam.flink;

import org.apache.beam.runners.flink.FlinkRunner;
import rocks.theodolite.benchmarks.commons.beam.BeamService;
import rocks.theodolite.benchmarks.uc3.beam.SimplePipelineFactory;

/**
 * Implementation of the use case Aggregation based on Time Attributes using Apache Beam with the
 * Flink Runner. To run locally in standalone start Kafka, Zookeeper, the schema-registry and the
 * workload generator using the delayed_startup.sh script. And configure the Kafka, Zookeeper and
 * Schema Registry urls accordingly. Start a Flink cluster and pass its REST adress
 * using--flinkMaster as run parameter. To persist logs add
 * ${workspace_loc:/uc4-application-samza/eclipseConsoleLogs.log} as Output File under Standard
 * Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc3BeamFlink {

  private Uc3BeamFlink() {}

  /**
   * Start running this microservice.
   */
  public static void main(final String[] args) {
    new BeamService(SimplePipelineFactory.factory(), FlinkRunner.class, args).run();
  }
}

