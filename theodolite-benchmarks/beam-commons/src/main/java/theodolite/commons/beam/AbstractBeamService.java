package theodolite.commons.beam;

import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.common.configuration.ServiceConfigurations;

/**
 * Abstraction of a beam microservice.
 * Encapsulates the corresponding {@link PipelineOptions} and the beam Runner.
 */
public class AbstractBeamService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBeamService.class);

  // Beam Pipeline
  protected PipelineOptions options;

  // Application Configurations
  private final Configuration config = ServiceConfigurations.createWithDefaults();
  private final String applicationName =
      config.getString(ConfigurationKeys.APPLICATION_NAME);


  /**
   * Creates AbstractBeamService with options.
   */
  public AbstractBeamService(final String[] args) { //NOPMD
    super();
    options = PipelineOptionsFactory.fromArgs(args).create();
    options.setJobName(applicationName);
    LOGGER.info("Starting BeamService with PipelineOptions {}:", this.options.toString());
  }

  public Configuration getConfig() {
    return config;
  }

}
