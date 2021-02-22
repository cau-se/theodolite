package theodolite.commons.workloadgeneration;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theodolite.commons.workloadgeneration.functions.MessageGenerator;

public class LoadGeneratorExecution {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadGeneratorExecution.class);

  private final Random random = new Random();
  private final WorkloadDefinition workloadDefinition;
  private final MessageGenerator messageGenerator;
  private final ScheduledExecutorService executor;

  public LoadGeneratorExecution(
      final WorkloadDefinition workloadDefinition,
      final MessageGenerator messageGenerator,
      final int threads) {
    this.workloadDefinition = workloadDefinition;
    this.messageGenerator = messageGenerator;
    this.executor = Executors.newScheduledThreadPool(threads);
  }

  public void start() {
    LOGGER.info("Beginning of Experiment...");
    LOGGER.info("Generating records for {} keys.",
        this.workloadDefinition.getKeySpace().getCount());
    LOGGER.info("Experiment is going to be executed until cancelation...");

    final int periodMs = (int) this.workloadDefinition.getPeriod().toMillis();
    for (final String key : this.workloadDefinition.getKeySpace().getKeys()) {
      final long initialDelay = this.random.nextInt(periodMs);
      final Runnable task = () -> this.messageGenerator.generate(key);
      this.executor.scheduleAtFixedRate(task, initialDelay, periodMs, TimeUnit.MILLISECONDS);
    }
  }

  public void stop() {
    this.executor.shutdownNow();
  }
}
