package application;

import org.apache.beam.runners.samza.SamzaRunner;
import theodolite.commons.beam.BeamService;

/**
 * Implementation of the use case Hierarchical Aggregation using Apache Beam with the Samza Runner.
 * To run locally in standalone start Kafka, Zookeeper, the schema-registry and the workload
 * generator using the delayed_startup.sh script. Add
 * --configFactory=org.apache.samza.config.factories.PropertiesConfigFactory
 * --configFilePath=${workspace_loc:uc4-application-samza}/config/standalone_local.properties
 * --samzaExecutionEnvironment=STANDALONE --maxSourceParallelism=1024 --as program arguments. To
 * persist logs add ${workspace_loc:/uc4-application-samza/eclipseConsoleLogs.log} as Output File
 * under Standard Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc4BeamSamza {

  private Uc4BeamSamza() {}

  /**
   * Start running this microservice.
   */
  public static void main(final String[] args) {
    new BeamService(PipelineFactory.factory(), SamzaRunner.class, args).run();
  }

}
