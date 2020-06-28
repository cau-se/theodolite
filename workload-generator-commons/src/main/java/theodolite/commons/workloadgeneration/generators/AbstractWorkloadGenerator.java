package theodolite.commons.workloadgeneration.generators;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import kieker.common.record.IMonitoringRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theodolite.commons.workloadgeneration.communication.zookeeper.WorkloadDistributor;
import theodolite.commons.workloadgeneration.dimensions.KeySpace;
import theodolite.commons.workloadgeneration.functions.BeforeAction;
import theodolite.commons.workloadgeneration.functions.MessageGenerator;
import theodolite.commons.workloadgeneration.functions.Transport;
import theodolite.commons.workloadgeneration.misc.WorkloadDefinition;
import theodolite.commons.workloadgeneration.misc.WorkloadEntity;
import theodolite.commons.workloadgeneration.misc.ZooKeeper;

/**
 * Base for workload generators.
 *
 * @param <T> The type of records the workload generator is dedicated for.
 */
public abstract class AbstractWorkloadGenerator<T extends IMonitoringRecord>
    implements WorkloadGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWorkloadGenerator.class);

  private final int instances;

  private final ZooKeeper zooKeeper;

  private final KeySpace keySpace;

  private final int threads;

  private final Duration period;

  private final Duration duration;

  private final BeforeAction beforeAction;

  private final BiFunction<WorkloadDefinition, Integer, List<WorkloadEntity<T>>> workloadSelector;

  private final MessageGenerator<T> generatorFunction;

  private final Transport<T> transport;

  private WorkloadDistributor workloadDistributor;

  private final ScheduledExecutorService executor;

  /**
   * Create a new workload generator.
   *
   * @param instances the number of workload-generator instances.
   * @param zooKeeper the zookeeper connection.
   * @param keySpace the keyspace.
   * @param threads the number of threads that is used to generate the load.
   * @param period the period, how often a new record is emitted.
   * @param duration the maximum runtime.
   * @param beforeAction the action to perform before the workload generation starts.
   * @param generatorFunction the function that is used to generate the individual records.
   * @param transport the function that is used to send generated messages to the messaging system.
   */
  public AbstractWorkloadGenerator(
      final int instances,
      final ZooKeeper zooKeeper,
      final KeySpace keySpace,
      final int threads,
      final Duration period,
      final Duration duration,
      final BeforeAction beforeAction,
      final MessageGenerator<T> generatorFunction,
      final Transport<T> transport) {
    this.instances = instances;
    this.zooKeeper = zooKeeper;
    this.period = period;
    this.threads = threads;
    this.keySpace = keySpace;
    this.duration = duration;
    this.beforeAction = beforeAction;
    this.generatorFunction = generatorFunction;
    this.workloadSelector = (workloadDefinition, workerId) -> {
      final List<WorkloadEntity<T>> workloadEntities = new LinkedList<>();

      for (int i =
          workloadDefinition.getKeySpace().getMin() + workerId; i <= workloadDefinition
              .getKeySpace().getMax(); i += workloadDefinition.getNumberOfWorkers()) {
        final String id = workloadDefinition.getKeySpace().getPrefix() + i;
        workloadEntities.add(new WorkloadEntity<>(id, this.generatorFunction));
      }

      return workloadEntities;
    };
    this.transport = transport;

    this.executor = Executors.newScheduledThreadPool(threads);
    final Random random = new Random();

    final int periodMs = (int) period.toMillis();

    LOGGER.info("Period: " + periodMs);

    final BiConsumer<WorkloadDefinition, Integer> workerAction = (declaration, workerId) -> {

      final List<WorkloadEntity<T>> entities = this.workloadSelector.apply(declaration, workerId);

      LOGGER.info("Beginning of Experiment...");
      LOGGER.info("Generating records for {} keys.", entities.size());
      LOGGER.info("Experiment is going to be executed for the specified duration...");

      entities.forEach(entity -> {
        final T message = entity.generateMessage();
        final long initialDelay = random.nextInt(periodMs);
        final Runnable task = () -> {
          this.transport.transport(message);
        };
        this.executor.scheduleAtFixedRate(task, initialDelay,
            periodMs, TimeUnit.MILLISECONDS);
      });


      try {
        this.executor.awaitTermination(duration.getSeconds(), TimeUnit.SECONDS);
        LOGGER.info("Terminating now...");
        this.stop();
      } catch (final InterruptedException e) {
        LOGGER.error("", e);
        throw new IllegalStateException("Error when terminating the workload generation.");
      }
    };

    this.workloadDistributor =
        new WorkloadDistributor(this.instances, this.zooKeeper, this.keySpace, this.beforeAction,
            workerAction);
  }

  /**
   * Start the workload generation. The generation terminates automatically after the specified
   * {@code duration}.
   */
  @Override
  public void start() {
    this.workloadDistributor.start();
  }

  @Override
  public void stop() {
    this.workloadDistributor.stop();
  }
}
