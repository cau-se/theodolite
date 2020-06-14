package theodolite.commons.workloadgeneration.generators;

import java.time.Duration;
import java.util.Objects;
import kieker.common.record.IMonitoringRecord;
import theodolite.commons.workloadgeneration.communication.kafka.KafkaRecordSender;
import theodolite.commons.workloadgeneration.dimensions.KeySpace;
import theodolite.commons.workloadgeneration.functions.BeforeAction;
import theodolite.commons.workloadgeneration.functions.MessageGenerator;
import theodolite.commons.workloadgeneration.misc.ZooKeeper;

public class KafkaWorkloadGeneratorBuilder<T extends IMonitoringRecord> {

  private int instances;

  private ZooKeeper zooKeeper;

  private KeySpace keySpace;

  private int threads;

  private Duration period;

  private Duration duration;

  private BeforeAction beforeAction;

  private MessageGenerator<T> generatorFunction;

  private KafkaRecordSender<T> kafkaRecordSender;

  private KafkaWorkloadGeneratorBuilder() {

  }

  /**
   * Get a builder for the {@link KafkaWorkloadGenerator}.
   *
   * @return the builder.
   */
  public static <T extends IMonitoringRecord> KafkaWorkloadGeneratorBuilder<T> builder() {
    return new KafkaWorkloadGeneratorBuilder<>();
  }

  /**
   * Set the number of instances.
   *
   * @param zooKeeper a reference to the ZooKeeper instance.
   * @return the builder.
   */
  public KafkaWorkloadGeneratorBuilder<T> setInstances(final int instances) {
    this.instances = instances;
    return this;
  }

  /**
   * Set the ZooKeeper reference.
   *
   * @param zooKeeper a reference to the ZooKeeper instance.
   * @return the builder.
   */
  public KafkaWorkloadGeneratorBuilder<T> setZooKeeper(final ZooKeeper zooKeeper) {
    this.zooKeeper = zooKeeper;
    return this;
  }

  /**
   * Set the before action for the {@link KafkaWorkloadGenerator}.
   *
   * @param beforeAction the {@link BeforeAction}.
   * @return the builder.
   */
  public KafkaWorkloadGeneratorBuilder<T> setBeforeAction(final BeforeAction beforeAction) {
    this.beforeAction = beforeAction;
    return this;
  }

  /**
   * Set the key space for the {@link KafkaWorkloadGenerator}.
   *
   * @param keySpace the {@link KeySpace}.
   * @return the builder.
   */
  public KafkaWorkloadGeneratorBuilder<T> setKeySpace(final KeySpace keySpace) {
    this.keySpace = keySpace;
    return this;
  }

  /**
   * Set the key space for the {@link KafkaWorkloadGenerator}.
   *
   * @param keySpace the {@link KeySpace}.
   * @return the builder.
   */
  public KafkaWorkloadGeneratorBuilder<T> setThreads(final int threads) {
    this.threads = threads;
    return this;
  }

  /**
   * Set the period for the {@link KafkaWorkloadGenerator}.
   *
   * @param period the {@link Period}
   * @return the builder.
   */
  public KafkaWorkloadGeneratorBuilder<T> setPeriod(final Duration period) {
    this.period = period;
    return this;
  }

  /**
   * Set the durtion for the {@link KafkaWorkloadGenerator}.
   *
   * @param duration the {@link Duration}.
   * @return the builder.
   */
  public KafkaWorkloadGeneratorBuilder<T> setDuration(final Duration duration) {
    this.duration = duration;
    return this;
  }

  /**
   * Set the generator function for the {@link KafkaWorkloadGenerator}.
   *
   * @param generatorFunction the generator function.
   * @return the builder.
   */
  public KafkaWorkloadGeneratorBuilder<T> setGeneratorFunction(
      final MessageGenerator<T> generatorFunction) {
    this.generatorFunction = generatorFunction;
    return this;
  }

  /**
   * Set the {@link KafkaRecordSender} for the {@link KafkaWorkloadGenerator}.
   *
   * @param kafkaRecordSender the record sender to use.
   * @return the builder.
   */
  public KafkaWorkloadGeneratorBuilder<T> setKafkaRecordSender(
      final KafkaRecordSender<T> kafkaRecordSender) {
    this.kafkaRecordSender = kafkaRecordSender;
    return this;
  }

  /**
   * Build the actual {@link KafkaWorkloadGenerator}. The following parameters are must be
   * specicified before this method is called:
   * <ul>
   * <li>zookeeper</li>
   * <li>key space</li>
   * <li>period</li>
   * <li>duration</li>
   * <li>generator function</li>
   * <li>kafka record sender</li>
   * </ul>
   *
   * @return the built instance of the {@link KafkaWorkloadGenerator}.
   */
  public KafkaWorkloadGenerator<T> build() {
    Objects.requireNonNull(this.instances, "Please specify the number of instances.");
    Objects.requireNonNull(this.zooKeeper, "Please specify the ZooKeeper instance.");
    this.threads = Objects.requireNonNullElse(this.threads, 1);
    Objects.requireNonNull(this.keySpace, "Please specify the key space.");
    Objects.requireNonNull(this.period, "Please specify the period.");
    Objects.requireNonNull(this.duration, "Please specify the duration.");
    this.beforeAction = Objects.requireNonNullElse(this.beforeAction, () -> {
    });
    Objects.requireNonNull(this.generatorFunction, "Please specify the generator function.");
    Objects.requireNonNull(this.kafkaRecordSender, "Please specify the kafka record sender.");

    return new KafkaWorkloadGenerator<>(
        this.instances,
        this.zooKeeper,
        this.keySpace,
        this.threads,
        this.period,
        this.duration,
        this.beforeAction,
        this.generatorFunction,
        this.kafkaRecordSender);
  }
}
