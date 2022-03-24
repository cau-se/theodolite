package rocks.theodolite.benchmarks.commons.beam;

import java.io.IOException;
import java.util.function.Function;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
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
  private PipelineResult pipelineResult;

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
   * Start this microservice by running the underlying Beam pipeline.
   */
  public void run() {
    LOGGER.info("Constructing Beam pipeline with pipeline options: {}",
        this.pipelineOptions.toString());
    final Pipeline pipeline = this.pipelineFactory.create(this.pipelineOptions);
    LOGGER.info("Starting BeamService {}.", this.applicationName);
    this.pipelineResult = pipeline.run();
  }

  /**
   * Start this microservice by running the underlying Beam pipeline and block until this process is
   * terminated.
   */
  public void runStandalone() {
    this.run();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> this.stop()));
    this.pipelineResult.waitUntilFinish();
  }

  /**
   * Stop this microservice by canceling the underlying Beam pipeline.
   */
  public void stop() {
    LOGGER.info("Initiate shutdown of Beam service {}.", this.applicationName);
    if (this.pipelineResult == null) {
      throw new IllegalStateException("Cannot stop service since it has never been started.");
    }
    LOGGER.info("Stoping Beam pipeline.");
    try {
      this.pipelineResult.cancel();
      this.pipelineResult = null; // NOPMD use null to indicate absence
    } catch (final IOException e) {
      throw new IllegalStateException(
          "Stoping the service failed due to failed stop of Beam pipeline.", e);
    }
    LOGGER.info("Shutdown of Beam service {} complete.", this.applicationName);
  }

}
