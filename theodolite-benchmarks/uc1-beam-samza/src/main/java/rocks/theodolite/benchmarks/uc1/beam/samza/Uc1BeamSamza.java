package rocks.theodolite.benchmarks.uc1.beam.samza;

import org.apache.beam.runners.samza.SamzaRunner;
import rocks.theodolite.benchmarks.commons.beam.BeamService;
import rocks.theodolite.benchmarks.uc1.beam.PipelineFactory;

/**
 * Implementation of the use case Database Storage using Apache Beam with the Samza Runner. To
 * execute locally in standalone start Kafka, Zookeeper, the schema-registry and the workload
 * generator. Add --configFactory=org.apache.samza.config.factories.PropertiesConfigFactory
 * --configFilePath=samza-standalone.properties --samzaExecutionEnvironment=STANDALONE
 * --maxSourceParallelism=1024 as program arguments. To persist logs add
 * ${workspace_loc:/uc4-application-samza/eclipseConsoleLogs.log} as Output File under Standard
 * Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc1BeamSamza {

  private Uc1BeamSamza() {}

  /**
   * Main method.
   */
  public static void main(final String[] args) {
    new BeamService(PipelineFactory.factory(), SamzaRunner.class, args).run();
  }
}
