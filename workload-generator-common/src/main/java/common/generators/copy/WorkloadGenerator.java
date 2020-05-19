package common.generators.copy;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import common.dimensions.Duration;
import common.dimensions.KeySpace;
import common.dimensions.Period;
import common.functions.BeforeAction;
import common.functions.MessageGenerator;
import common.functions.Transport;
import common.messages.OutputMessage;
import common.misc.Worker;
import common.misc.WorkloadDefinition;
import common.misc.WorkloadEntity;
import communication.zookeeper.WorkloadDistributor;
import kieker.common.record.IMonitoringRecord;

public abstract class WorkloadGenerator<T extends IMonitoringRecord> implements IWorkloadGenerator {

  private final KeySpace keySpace;

  private final Period period;

  private final Duration duration;

  private final BeforeAction beforeAction;

  private final BiFunction<WorkloadDefinition, Worker, List<WorkloadEntity<T>>> workloadSelector;

  private final MessageGenerator<T> generatorFunction;

  private final Transport<T> transport;

  private WorkloadDistributor workloadDistributor;

  private final ScheduledExecutorService executor;

  /**
   * Start the workload generation. The generation terminates automatically after the specified
   * {@code duration}.s
   */
  @Override
  public void start() {
    this.workloadDistributor.start();
  }

  @Override
  public void stop() {
    this.workloadDistributor.stop();
  }

  public WorkloadGenerator(
      final KeySpace keySpace,
      final Period period,
      final Duration duration,
      final BeforeAction beforeAction,
      final MessageGenerator<T> generatorFunction,
      final Transport<T> transport) {
    this.period = period;
    this.keySpace = keySpace;
    this.duration = duration;
    this.beforeAction = beforeAction;
    this.generatorFunction = generatorFunction;
    this.workloadSelector = (workloadDeclaration, worker) -> {
      final List<WorkloadEntity<T>> workloadEntities = new LinkedList<>();

      for (int i =
          workloadDeclaration.getKeySpace().getMin() + worker.getId(); i <= workloadDeclaration
              .getKeySpace().getMax(); i += workloadDeclaration.getNumberOfWorkers()) {
        final String id = workloadDeclaration.getKeySpace().getPrefix() + i;
        workloadEntities.add(new WorkloadEntity<>(id, this.generatorFunction));
      }

      return workloadEntities;
    };
    this.transport = transport;

    final int threads = 10; // env
    this.executor = Executors.newScheduledThreadPool(threads);
    final Random random = new Random();

    final int periodMs = period.getDuration();

    final BiConsumer<WorkloadDefinition, Worker> workerAction = (declaration, worker) -> {

      final List<WorkloadEntity<T>> entities = this.workloadSelector.apply(declaration, worker);

      System.out.println("Beginning of Experiment...");
      System.out.println("Experiment is going to be executed for the specified duration...");
      entities.forEach(entity -> {
        final OutputMessage<T> message = entity.generateMessage();
        final long initialDelay = random.nextInt(periodMs);
        this.executor.scheduleAtFixedRate(() -> this.transport.transport(message), initialDelay,
            periodMs, period.getTimeUnit());

      });

      try {
        this.executor.awaitTermination(duration.getDuration(), duration.getTimeUnit());
        System.out.println("Terminating now...");
        this.stop();
      } catch (final InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    };

    this.workloadDistributor =
        new WorkloadDistributor(this.keySpace, this.beforeAction, workerAction);
  }
}
