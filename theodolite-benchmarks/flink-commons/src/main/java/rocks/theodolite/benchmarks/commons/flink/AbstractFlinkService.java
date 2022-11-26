package rocks.theodolite.benchmarks.commons.flink;

import org.apache.commons.configuration2.Configuration;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.common.configuration.ServiceConfigurations;

/**
 * A general Apache Flink-based microservice. It is configured by {@link #configureEnv()}, and
 * extended by implementing business logic in {@link #buildPipeline()}. The configuration of the
 * serializer needs to be implemented in {@link #configureSerializers()}.
 */
public abstract class AbstractFlinkService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFlinkService.class);
  protected final StreamExecutionEnvironment env;

  protected Configuration config = ServiceConfigurations.createWithDefaults();

  protected final String applicationId;

  /**
   * Abstract Service constructing the name and {@link StreamExecutionEnvironment}.
   */
  public AbstractFlinkService() {
    final String applicationName = this.config.getString(ConfigurationKeys.APPLICATION_NAME);
    final String applicationVersion = this.config.getString(ConfigurationKeys.APPLICATION_VERSION);
    this.applicationId = applicationName + "-" + applicationVersion;

    this.env = StreamExecutionEnvironment.getExecutionEnvironment();

  }

  /**
   * Abstract Service constructing the name and {@link StreamExecutionEnvironment}.
   *
   * @param config the configuration for the service.
   */
  public AbstractFlinkService(final Configuration config) {
    this.config = config;
    final String applicationName = this.config.getString(ConfigurationKeys.APPLICATION_NAME);
    final String applicationVersion = this.config.getString(ConfigurationKeys.APPLICATION_VERSION);
    this.applicationId = applicationName + "-" + applicationVersion;
    this.env = StreamExecutionEnvironment.getExecutionEnvironment();
  }



  /**
   * Configures the service using environment variables.
   */
  protected void configureEnv() {
    this.configureCheckpointing();
    this.configureParallelism();
    this.configureStateBackend();
    this.configureSerializers();
  }

  protected void configureCheckpointing() {
    final boolean checkpointing = this.config.getBoolean(ConfigurationKeys.CHECKPOINTING, true);
    final int commitIntervalMs = this.config.getInt(ConfigurationKeys.CHECKPOINTING_INTERVAL_MS);
    LOGGER.info("Set parallelism to: {}.", checkpointing);
    if (checkpointing) {
      this.env.enableCheckpointing(commitIntervalMs);
    }
  }

  /**
   * Configures the parallelism according to the configuration.
   */
  protected void configureParallelism() {
    final Integer parallelism = this.config.getInteger(ConfigurationKeys.PARALLELISM, null);
    if (parallelism != null) {
      LOGGER.info("Set parallelism: {}.", parallelism);
      this.env.setParallelism(parallelism);
    }
  }

  /**
   * Configures the state backend according to the configuration.
   */
  public void configureStateBackend() {
    LOGGER.info("Enable state backend.");
    final StateBackend stateBackend = StateBackends.fromConfiguration(this.config);
    this.env.setStateBackend(stateBackend);
  }


  protected abstract void configureSerializers();

  /**
   * Empty placeholder. Implement this method to implement the custom logic of your microservice.
   */
  protected abstract void buildPipeline();

  /**
   * Starts the service.
   */
  public void run() {
    this.configureEnv();
    this.buildPipeline();
    LOGGER.info("Execution plan: {}", this.env.getExecutionPlan());

    try {
      this.env.execute(this.applicationId);
    } catch (final Exception e) { // NOPMD Exception thrown by Flink
      LOGGER.error("An error occured while running this job.", e);
    }
  }


}
