package rocks.theodolite.benchmarks.uc1.beam.flink;

import org.apache.beam.runners.flink.FlinkRunner;
import rocks.theodolite.benchmarks.commons.beam.BeamService;
import rocks.theodolite.benchmarks.uc1.beam.PipelineFactory;

/**
 * Implementation of the use case Database Storage using Apache Beam with the Flink Runner. To
 * execute locally in standalone start Kafka, Zookeeper, the schema-registry and the workload
 * generator using the delayed_startup.sh script. Start a Flink cluster and pass its REST adress
 * using--flinkMaster as run parameter. To persist logs add
 * ${workspace_loc:/uc1-application-samza/eclipseConsoleLogs.log} as Output File under Standard
 * Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc1BeamFlink {

  private Uc1BeamFlink() {}

  public static void main(final String[] args) {
    new BeamService(PipelineFactory.factory(), FlinkRunner.class, args).runStandalone();
  }

}

