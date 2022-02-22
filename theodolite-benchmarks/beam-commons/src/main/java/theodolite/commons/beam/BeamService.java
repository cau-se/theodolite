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

public class BeamService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BeamService.class);

  private final Configuration config = ServiceConfigurations.createWithDefaults();
  private final String applicationName = this.config.getString(ConfigurationKeys.APPLICATION_NAME);

  private final AbstractPipelineFactory pipelineFactory;
  private final PipelineOptions pipelineOptions;

  public BeamService(
      Function<Configuration, AbstractPipelineFactory> pipelineFactoryFactory,
      Class<? extends PipelineRunner<?>> runner,
      String[] args) {
    this.pipelineFactory = pipelineFactoryFactory.apply(this.config);
    this.pipelineOptions = PipelineOptionsFactory.fromArgs(args).create();
    this.pipelineOptions.setJobName(this.applicationName);
    this.pipelineOptions.setRunner(runner);
  }

  public void run() {
    LOGGER.info("Construct Beam pipeline with pipeline options: {}",
        this.pipelineOptions.toString());
    final Pipeline pipeline = this.pipelineFactory.create(this.pipelineOptions);
    LOGGER.info("Starting BeamService {}.", this.applicationName);
    pipeline.run().waitUntilFinish();
  }

}
