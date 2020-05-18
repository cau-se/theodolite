package communication.zookeeper.leader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import common.Worker;
import common.WorkloadDeclaration;
import common.dimensions.KeySpace;

public class WorkloadDistributor {
  
  
  private static final String COUNTER_PATH = "/counter";
  private static final String WORKLOAD_PATH = "/workload";
  private static final String WORKLOAD_DEFINITION_PATH = "/workload/definition";
  
  private final DistributedAtomicInteger counter;
  
  private final KeySpace keySpace;
  private final BiConsumer<WorkloadDeclaration, Worker> workerAction;
  
  private final CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new RetryNTimes(3, 1000));
  

  
  public WorkloadDistributor(final KeySpace keySpace, final BiConsumer<WorkloadDeclaration, Worker> workerAction) {
    
    this.keySpace = keySpace;
    this.workerAction = workerAction;
    
    client.start();
    
    try {
      client.blockUntilConnected();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    counter = new DistributedAtomicInteger(client, COUNTER_PATH, new RetryNTimes(3, 1000));
  }
  
  public void start() {
    try {
      AtomicValue<Integer> result = counter.increment();
      final int id = result.preValue();
      if (result.succeeded()) {
        
        CuratorWatcher watcher = this.buildWatcher(id);
        
        client.checkExists().creatingParentsIfNeeded().forPath(WORKLOAD_DEFINITION_PATH);

        if (id == 0) {
          System.out.println("is master");
                    
          // register worker action, as master acts also as worker
          client.getChildren().usingWatcher(watcher).forPath(WORKLOAD_PATH);
          
          
          Thread.sleep(10000); // wait for all workers to participate in the leader election
          
          int numberOfWorkers = this.counter.get().postValue();
          
          System.out.printf("Number of Workers: %d\n", numberOfWorkers);
          
          final WorkloadDeclaration declaration = new WorkloadDeclaration(this.keySpace, numberOfWorkers);
          
          client.create().forPath(WORKLOAD_DEFINITION_PATH, declaration.toString().getBytes(StandardCharsets.UTF_8));
                              
        } else {
          System.out.println("is worker");
          
          client.getChildren().usingWatcher(watcher).forPath(WORKLOAD_PATH);
        }
        
        Thread.sleep(20000); // wait until the workload declaration is retrieved

      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private CuratorWatcher buildWatcher(int id) {
    return new CuratorWatcher() {
      
      @Override
      public void process(WatchedEvent event) throws Exception {
        if(event.getType() == EventType.NodeChildrenChanged) {
          byte[] bytes = client.getData().forPath(WORKLOAD_DEFINITION_PATH);
          final WorkloadDeclaration declaration = WorkloadDeclaration.fromString(new String(bytes, StandardCharsets.UTF_8));
          
          if (id > declaration.getNumberOfWorkers() - 1) {
            throw new IllegalStateException("Worker with id " + id + " was too slow and is therefore not participating in the workload generation.");
          } else {
            workerAction.accept(declaration, new Worker(id));
          }
        }
      }
    };
  }

  public void stop() {
    this.client.close();
  }

}
