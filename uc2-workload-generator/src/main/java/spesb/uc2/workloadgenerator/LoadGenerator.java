package spesb.uc2.workloadgenerator;

import common.dimensions.Duration;
import common.dimensions.KeySpace;
import common.dimensions.Period;
import common.generators.KafkaWorkloadGenerator;
import common.generators.KafkaWorkloadGeneratorBuilder;
import common.misc.ZooKeeper;
import communication.kafka.KafkaRecordSender;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.configuration.events.Event;
import titan.ccp.model.sensorregistry.MutableAggregatedSensor;
import titan.ccp.model.sensorregistry.MutableSensorRegistry;
import titan.ccp.models.records.ActivePowerRecord;

public class LoadGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadGenerator.class);

  public static void main(final String[] args) throws InterruptedException, IOException {
    // uc2
    LOGGER.info("Start workload generator for use case UC2.");

    // get environment variables
    final String hierarchy = Objects.requireNonNullElse(System.getenv("HIERARCHY"), "deep");
    final int numNestedGroups = Integer
        .parseInt(Objects.requireNonNullElse(System.getenv("NUM_NESTED_GROUPS"), "1"));
    final String zooKeeperHost = Objects.requireNonNullElse(System.getenv("ZK_HOST"), "localhost");
    final int zooKeeperPort =
        Integer.parseInt(Objects.requireNonNullElse(System.getenv("ZK_PORT"), "2181"));
    final int numSensors =
        Integer.parseInt(Objects.requireNonNullElse(System.getenv("NUM_SENSORS"), "1"));
    final int periodMs =
        Integer.parseInt(Objects.requireNonNullElse(System.getenv("PERIOD_MS"), "1000"));
    final int value = Integer.parseInt(Objects.requireNonNullElse(System.getenv("VALUE"), "10"));
    final boolean sendRegistry = Boolean
        .parseBoolean(Objects.requireNonNullElse(System.getenv("SEND_REGISTRY"), "true"));
    final int threads = Integer.parseInt(Objects.requireNonNullElse(System.getenv("THREADS"), "4"));
    final String kafkaBootstrapServers =
        Objects.requireNonNullElse(System.getenv("KAFKA_BOOTSTRAP_SERVERS"),
            "localhost:9092");
    final String kafkaInputTopic =
        Objects.requireNonNullElse(System.getenv("KAFKA_INPUT_TOPIC"), "input");
    final String kafkaBatchSize = System.getenv("KAFKA_BATCH_SIZE");
    final String kafkaLingerMs = System.getenv("KAFKA_LINGER_MS");
    final String kafkaBufferMemory = System.getenv("KAFKA_BUFFER_MEMORY");

    // build sensor registry
    final MutableSensorRegistry sensorRegistry =
        buildSensorRegistry(hierarchy, numNestedGroups, numSensors);

    // create kafka record sender
    final Properties kafkaProperties = new Properties();
    // kafkaProperties.put("acks", this.acknowledges);
    kafkaProperties.compute(ProducerConfig.BATCH_SIZE_CONFIG, (k, v) -> kafkaBatchSize);
    kafkaProperties.compute(ProducerConfig.LINGER_MS_CONFIG, (k, v) -> kafkaLingerMs);
    kafkaProperties.compute(ProducerConfig.BUFFER_MEMORY_CONFIG, (k, v) -> kafkaBufferMemory);
    final KafkaRecordSender<ActivePowerRecord> kafkaRecordSender =
        new KafkaRecordSender<>(kafkaBootstrapServers,
            kafkaInputTopic, r -> r.getIdentifier(), r -> r.getTimestamp(), kafkaProperties);

    // create workload generator
    final KafkaWorkloadGenerator<ActivePowerRecord> workloadGenerator =
        KafkaWorkloadGeneratorBuilder.<ActivePowerRecord>builder()
            .setKeySpace(new KeySpace("s_", numSensors))
            .setThreads(threads)
            .setPeriod(new Period(periodMs, TimeUnit.MILLISECONDS))
            .setDuration(new Duration(30, TimeUnit.DAYS))
            .setBeforeAction(() -> {
              if (sendRegistry) {
                final ConfigPublisher configPublisher =
                    new ConfigPublisher(kafkaBootstrapServers, "configuration");
                configPublisher.publish(Event.SENSOR_REGISTRY_CHANGED, sensorRegistry.toJson());
                configPublisher.close();
                System.out.println("Configuration sent.");

                System.out.println("Now wait 30 seconds");
                try {
                  Thread.sleep(30_000);
                } catch (final InterruptedException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
                System.out.println("And woke up again :)");
              }
            })
            .setGeneratorFunction(
                sensor -> new ActivePowerRecord(sensor, System.currentTimeMillis(), value))
            .setZooKeeper(new ZooKeeper(zooKeeperHost, zooKeeperPort))
            .setKafkaRecordSender(kafkaRecordSender)
            .build();

    // start
    workloadGenerator.start();
  }

  private static MutableSensorRegistry buildSensorRegistry(
      final String hierarchy,
      final int numNestedGroups,
      final int numSensors) {
    final MutableSensorRegistry sensorRegistry = new MutableSensorRegistry("group_lvl_0");
    if (hierarchy.equals("deep")) {
      MutableAggregatedSensor lastSensor = sensorRegistry.getTopLevelSensor();
      for (int lvl = 1; lvl < numNestedGroups; lvl++) {
        lastSensor = lastSensor.addChildAggregatedSensor("group_lvl_" + lvl);
      }
      for (int s = 0; s < numSensors; s++) {
        lastSensor.addChildMachineSensor("sensor_" + s);
      }
    } else if (hierarchy.equals("full")) {
      addChildren(sensorRegistry.getTopLevelSensor(), numSensors, 1, numNestedGroups, 0);
    } else {
      throw new IllegalStateException();
    }
    return sensorRegistry;
  }

  private static int addChildren(final MutableAggregatedSensor parent, final int numChildren,
      final int lvl,
      final int maxLvl, int nextId) {
    for (int c = 0; c < numChildren; c++) {
      if (lvl == maxLvl) {
        parent.addChildMachineSensor("s_" + nextId);
        nextId++;
      } else {
        final MutableAggregatedSensor newParent =
            parent.addChildAggregatedSensor("g_" + lvl + '_' + nextId);
        nextId++;
        nextId = addChildren(newParent, numChildren, lvl + 1, maxLvl, nextId);
      }
    }
    return nextId;
  }

}
