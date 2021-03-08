package theodolite.uc3.workloadgenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load generator for Theodolite use case UC3.
 */
public final class LoadGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadGenerator.class);

  private LoadGenerator() {
    throw new UnsupportedOperationException();
  }

  public static void main(final String[] args) {
    LOGGER.info("Start workload generator for use case UC3");
    theodolite.commons.workloadgeneration.LoadGenerator.fromEnvironment().run();
  }

}
