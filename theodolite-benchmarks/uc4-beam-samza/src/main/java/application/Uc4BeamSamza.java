package application;


import org.apache.beam.runners.samza.SamzaRunner;
import org.apache.beam.sdk.Pipeline;
import theodolite.commons.beam.AbstractBeamService;

/**
 * Implementation of the use case Hierarchical Aggregation using Apache Beam with the Samza
 * Runner. To run locally in standalone start Kafka, Zookeeper, the schema-registry and the
 * workload generator using the delayed_startup.sh script. Add
 * --configFactory=org.apache.samza.config.factories.PropertiesConfigFactory
 * --configFilePath=${workspace_loc:uc4-application-samza}/config/standalone_local.properties
 * --samzaExecutionEnvironment=STANDALONE --maxSourceParallelism=1024 --as program arguments. To
 * persist logs add ${workspace_loc:/uc4-application-samza/eclipseConsoleLogs.log} as Output File
 * under Standard Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc4BeamSamza extends AbstractBeamService {


  /**
   * Private constructor setting specific options for this use case.
   */
  private Uc4BeamSamza(final String[] args) { //NOPMD
    super(args);
    this.options.setRunner(SamzaRunner.class);
  }

  /**
   * Start running this microservice.
   */
  @SuppressWarnings({"serial", "unchecked", "rawtypes"})
  public static void main(final String[] args) {

    final Uc4BeamSamza uc4BeamSamza = new Uc4BeamSamza(args);

    final Pipeline pipeline = new Uc4BeamPipeline(uc4BeamSamza.options, uc4BeamSamza.getConfig());

    pipeline.run().waitUntilFinish();
  }

}
