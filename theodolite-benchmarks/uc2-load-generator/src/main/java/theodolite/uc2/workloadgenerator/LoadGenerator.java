package theodolite.uc2.workloadgenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load generator for Theodolite use case UC2.
 */
public final class LoadGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadGenerator.class);

  private LoadGenerator() {}

  public static void main(final String[] args) {
    LOGGER.info("Start workload generator for use case UC2");
    rocks.theodolite.benchmarks.loadgenerator.LoadGenerator.fromEnvironment().run();
  }
}
