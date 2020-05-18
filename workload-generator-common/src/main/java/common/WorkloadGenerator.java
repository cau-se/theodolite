package common;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import common.dimensions.Duration;
import common.dimensions.KeySpace;
import common.dimensions.Period;
import common.functions.BeforeAction;
import common.functions.MessageGenerator;
import common.functions.Transport;
import common.messages.OutputMessage;
import communication.zookeeper.leader.WorkloadDistributor;

public abstract class WorkloadGenerator implements IWorkloadGenerator {
  
  private final KeySpace keySpace;
  
  private final Period period;
  
  private final Duration duration;
    
  private final BeforeAction beforeAction;
  
  private final BiFunction<WorkloadDeclaration, Worker, List<WorkloadEntity>> workloadSelector;
  
  private final MessageGenerator generatorFunction;
    
  private final Transport transport; // connect with previous stage by generating workloadentities from workloadranges / exchange via ZK
  
  private WorkloadDistributor workloadDistributor;
  
  private final ScheduledExecutorService executor;
  
  @Override
  public void start() {
    this.workloadDistributor.start();
  }

  @Override
  public void stop() {
    this.workloadDistributor.stop();
    this.executor.shutdown();
  }

  public WorkloadGenerator(
      final KeySpace keySpace,
      final Period period,
      final Duration duration,
      final BeforeAction beforeHook,
      final MessageGenerator generatorFunction,
      final Transport transport
      ) {
    this.period = period;
    this.keySpace = keySpace;
    this.duration = duration;
    this.beforeAction = beforeHook;
    this.generatorFunction = generatorFunction;
    this.workloadSelector = (workloadDeclaration, worker) -> {
      final List<WorkloadEntity> workloadEntities = new LinkedList<>();
      
      // construct workload entities of the subspace, this worker is accountable for
      // counting modulo #of workers with offset of the current worker id (worker ids starting at 0)
      for (int i = workloadDeclaration.getKeySpace().getMin() + worker.getId(); i <= workloadDeclaration.getKeySpace().getMax(); i+=workloadDeclaration.getNumberOfWorkers()) {
        final String id = workloadDeclaration.getKeySpace().getPrefix() + i;
        workloadEntities.add(new WorkloadEntity(id, this.generatorFunction));
      }
      
      return workloadEntities;
    };
    this.transport = transport;
            
    final int threads = 10; // env
    this.executor = Executors.newScheduledThreadPool(threads);
    final Random random = new Random();
    
    final int periodMs = period.getDuration();
    
    final BiConsumer<WorkloadDeclaration, Worker> workerAction = (declaration, worker) -> {
      
      List<WorkloadEntity> entities = this.workloadSelector.apply(declaration, worker);
  
      System.out.println("Beginning of Experiment...");
      entities.forEach(entity -> {
        final OutputMessage message = entity.generateMessage();
        final long initialDelay = random.nextInt(periodMs);
        executor.scheduleAtFixedRate(() -> this.transport.consume(message), initialDelay, periodMs, period.getTimeUnit());
  
      });
      
      try {
        System.out.println("Experiment is going to be executed for the specified duration...");
        executor.awaitTermination(duration.getDuration(), duration.getTimeUnit());
        System.out.println("Terminating now...");
        this.stop();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }      
    };
    
    this.workloadDistributor = new WorkloadDistributor(this.keySpace, workerAction);
  }
}
