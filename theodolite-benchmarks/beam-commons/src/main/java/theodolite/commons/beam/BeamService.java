package theodolite.commons.beam;

import java.util.function.Function;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineRunner;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.common.configuration.ServiceConfigurations;

/**
 * A general Apache Beam-based microservice. It is configured by Beam pipeline, a Beam runner and
 * additional configuration.
 */
public class BeamService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BeamService.class);

  private final Configuration config = ServiceConfigurations.createWithDefaults();
  private final String applicationName = this.config.getString(ConfigurationKeys.APPLICATION_NAME);

  private final AbstractPipelineFactory pipelineFactory;
  private final PipelineOptions pipelineOptions;

  /**
   * Create a new {@link BeamService}.
   *
   * @param pipelineFactoryFactory {@link Function} for creating an {@link AbstractPipelineFactory}
   *        based on a {@link Configuration}.
   * @param runner The Beam {@link PipelineRunner} to run this pipeline.
   * @param args Arguments which are treated as {@link PipelineOptions}.
   */
  public BeamService(
      final Function<Configuration, AbstractPipelineFactory> pipelineFactoryFactory,
      final Class<? extends PipelineRunner<?>> runner,
      final String... args) {
    this.pipelineFactory = pipelineFactoryFactory.apply(this.config);
    this.pipelineOptions = PipelineOptionsFactory.fromArgs(args).create();
    this.pipelineOptions.setJobName(this.applicationName);
    this.pipelineOptions.setRunner(runner);
  }

  /**
   * Start this microservice, by running the underlying Beam pipeline.
   */
  public void run() {
    LOGGER.info("Construct Beam pipeline with pipeline options: {}",
        this.pipelineOptions.toString());
    final Pipeline pipeline = this.pipelineFactory.create(this.pipelineOptions);
    LOGGER.info("Starting BeamService {}.", this.applicationName);
    pipeline.run().waitUntilFinish();
  }

}
