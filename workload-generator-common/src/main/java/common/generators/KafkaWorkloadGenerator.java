package common.generators;

import common.dimensions.Duration;
import common.dimensions.KeySpace;
import common.dimensions.Period;
import common.functions.BeforeAction;
import common.functions.MessageGenerator;
import common.misc.ZooKeeper;
import communication.kafka.KafkaRecordSender;
import kieker.common.record.IMonitoringRecord;

/**
 * Workload generator for generating load for the kafka messaging system.
 */
public class KafkaWorkloadGenerator<T extends IMonitoringRecord> extends WorkloadGenerator<T> {

  private final KafkaRecordSender<T> recordSender;

  /**
   * Create a new workload generator.
   *
   * @param zooKeeper a reference to the ZooKeeper instance.
   * @param keySpace the key space to generate the workload for.
   * @param threads tha amount of threads to use per instance.
   * @param period the period how often a message is generated for each key specified in the
   *        {@code keySpace}
   * @param duration the duration how long the workload generator will emit messages.
   * @param beforeAction the action which will be performed before the workload generator starts
   *        generating messages. If {@code null}, no before action will be performed.
   * @param generatorFunction the generator function. This function is executed, each time a message
   *        is generated.
   * @param recordSender the record sender which is used to send the generated messages to kafka.
   */
  public KafkaWorkloadGenerator(
      final ZooKeeper zooKeeper,
      final KeySpace keySpace,
      final int threads,
      final Period period,
      final Duration duration,
      final BeforeAction beforeAction,
      final MessageGenerator<T> generatorFunction,
      final KafkaRecordSender<T> recordSender) {
    super(zooKeeper, keySpace, threads, period, duration, beforeAction, generatorFunction,
        recordSender);
    this.recordSender = recordSender;
  }


  @Override
  public void stop() {
    this.recordSender.terminate();

    super.stop();
  }
}
