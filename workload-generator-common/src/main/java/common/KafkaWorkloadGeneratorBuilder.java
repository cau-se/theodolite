package common;

import java.util.Objects;
import common.dimensions.Duration;
import common.dimensions.KeySpace;
import common.dimensions.Period;
import common.functions.BeforeAction;
import common.functions.MessageGenerator;
import communication.kafka.KafkaRecordSender;
import titan.ccp.models.records.ActivePowerRecord;

public class KafkaWorkloadGeneratorBuilder {
        
  private KeySpace keySpace;
  
  private Period period;
  
  private Duration duration;
  
  private BeforeAction beforeAction;
  
  private MessageGenerator generatorFunction;
  
  private KafkaRecordSender<ActivePowerRecord> kafkaRecordSender;
    
  private KafkaWorkloadGeneratorBuilder() {
    
  }
  
  public static KafkaWorkloadGeneratorBuilder builder() {
    return new KafkaWorkloadGeneratorBuilder();
  }
  
  public KafkaWorkloadGeneratorBuilder setBeforeHook(final BeforeAction beforeAction) {
    this.beforeAction = beforeAction;
    return this;
  }
  
  public KafkaWorkloadGeneratorBuilder setKeySpace(final KeySpace keySpace) {
    this.keySpace = keySpace;
    return this;
  }
  
  public KafkaWorkloadGeneratorBuilder setPeriod(final Period period) {
    this.period = period;
    return this;
  }
  
  public KafkaWorkloadGeneratorBuilder setDuration(final Duration duration) {
    this.duration = duration;
    return this;
  }
  
  public KafkaWorkloadGeneratorBuilder setGeneratorFunction(final MessageGenerator generatorFunction) {
    this.generatorFunction = generatorFunction;
    return this;
  }
  
  public KafkaWorkloadGeneratorBuilder setKafkaRecordSender(final KafkaRecordSender<ActivePowerRecord> kafkaRecordSender) {
    this.kafkaRecordSender = kafkaRecordSender;
    return this;
  }
  
  public KafkaWorkloadGenerator build() {
    Objects.requireNonNull(this.keySpace, "Please specify the key space.");
    Objects.requireNonNull(this.period, "Please specify the period.");
    Objects.requireNonNull(this.duration, "Please specify the duration.");
    final BeforeAction beforeAction = Objects.requireNonNullElse(this.beforeAction, () -> {});
    Objects.requireNonNull(this.generatorFunction, "Please specify the generator function.");
    //Objects.requireNonNull(this.kafkaRecordSender, "Please specify the kafka record sender.");
            
    return new KafkaWorkloadGenerator(this.keySpace, this.period, this.duration, beforeAction, this.generatorFunction, this.kafkaRecordSender);
  }
}
