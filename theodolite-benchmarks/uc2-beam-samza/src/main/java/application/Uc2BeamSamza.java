package application;

import org.apache.beam.runners.samza.SamzaRunner;
import theodolite.commons.beam.BeamService;

/**
 * Implementation of the use case Downsampling using Apache Beam with the Samza Runner. To run
 * locally in standalone start Kafka, Zookeeper, the schema-registry and the workload generator
 * using the delayed_startup.sh script. Add
 * --configFactory=org.apache.samza.config.factories.PropertiesConfigFactory
 * --configFilePath=${workspace_loc:uc3-application-samza}/config/standalone_local.properties
 * --samzaExecutionEnvironment=STANDALONE --maxSourceParallelism=1024 --as program arguments. To
 * persist logs add ${workspace_loc:/uc3-application-samza/eclipseConsoleLogs.log} as Output File
 * under Standard Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc2BeamSamza {

  private Uc2BeamSamza() {}

  public static void main(final String[] args) {
    new BeamService(PipelineFactory.factory(), SamzaRunner.class, args).run();
  }

}

