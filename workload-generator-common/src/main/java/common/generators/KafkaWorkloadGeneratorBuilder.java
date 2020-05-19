package common.generators;

import java.util.Objects;
import common.dimensions.Duration;
import common.dimensions.KeySpace;
import common.dimensions.Period;
import common.functions.BeforeAction;
import common.functions.MessageGenerator;
import communication.kafka.KafkaRecordSender;
import kieker.common.record.IMonitoringRecord;

public class KafkaWorkloadGeneratorBuilder<T extends IMonitoringRecord> {

  private KeySpace keySpace;

  private Period period;

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
  public static KafkaWorkloadGeneratorBuilder<IMonitoringRecord> builder() {
    return new KafkaWorkloadGeneratorBuilder<>();
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
   * Set the period for the {@link KafkaWorkloadGenerator}.
   *
   * @param period the {@link Period}
   * @return the builder.
   */
  public KafkaWorkloadGeneratorBuilder<T> setPeriod(final Period period) {
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
    Objects.requireNonNull(this.keySpace, "Please specify the key space.");
    Objects.requireNonNull(this.period, "Please specify the period.");
    Objects.requireNonNull(this.duration, "Please specify the duration.");
    final BeforeAction beforeAction = Objects.requireNonNullElse(this.beforeAction, () -> {
    });
    Objects.requireNonNull(this.generatorFunction, "Please specify the generator function.");
    // Objects.requireNonNull(this.kafkaRecordSender, "Please specify the kafka record sender.");

    return new KafkaWorkloadGenerator<>(this.keySpace, this.period, this.duration, beforeAction,
        this.generatorFunction, this.kafkaRecordSender);
  }
}
