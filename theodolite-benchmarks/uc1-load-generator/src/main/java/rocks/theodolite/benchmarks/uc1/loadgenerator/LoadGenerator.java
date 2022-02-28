package rocks.theodolite.benchmarks.uc1.loadgenerator;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load Generator for Theodolite use case UC1.
 */
public final class LoadGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadGenerator.class);

  private LoadGenerator() {}

  /**
   * Start load generator for use case UC1.
   */
  public static void main(final String[] args) throws InterruptedException, IOException {
    LOGGER.info("Start workload generator for use case UC1.");
    rocks.theodolite.benchmarks.loadgenerator.LoadGenerator.fromEnvironment().run();
  }
}
