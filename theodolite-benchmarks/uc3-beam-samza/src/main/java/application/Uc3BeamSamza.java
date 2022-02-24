package application;

import org.apache.beam.runners.samza.SamzaRunner;
import theodolite.commons.beam.BeamService;

/**
 * Implementation of the use case Aggregation based on Time Attributes using Apache Beam with the
 * Samza Runner. To run locally in standalone start Kafka, Zookeeper, the schema-registry and the
 * workload generator using the delayed_startup.sh script. And configure the Kafka, Zookeeper and
 * Schema Registry urls accordingly. Start a Flink cluster and pass its REST adress
 * using--flinkMaster as run parameter. To persist logs add
 * ${workspace_loc:/uc4-application-samza/eclipseConsoleLogs.log} as Output File under Standard
 * Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc3BeamSamza {

  private Uc3BeamSamza() {}

  /**
   * Start running this microservice.
   */
  public static void main(final String[] args) {
    new BeamService(PipelineFactory.factory(), SamzaRunner.class, args).run();
  }

}

