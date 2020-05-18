package common;

import common.dimensions.Duration;
import common.dimensions.KeySpace;
import common.dimensions.Period;
import common.functions.BeforeAction;
import common.functions.MessageGenerator;
import communication.kafka.KafkaRecordSender;
import titan.ccp.models.records.ActivePowerRecord;

public class KafkaWorkloadGenerator extends WorkloadGenerator {
  
  private final KafkaRecordSender<ActivePowerRecord> recordSender;
  
  public KafkaWorkloadGenerator(
      final KeySpace keySpace,
      final Period period,
      final Duration duration,
      final BeforeAction beforeHook,
      final MessageGenerator generatorFunction,
      final KafkaRecordSender<ActivePowerRecord> recordSender
      ) {
    super(keySpace, period, duration, beforeHook, generatorFunction, outputMessage -> {
      //recordSender.write(outputMessage.getValue()); removed for dev
      System.out.println(outputMessage.getKey());
    });
    this.recordSender = recordSender;
  }

  
  @Override
  public void stop() {
    System.out.println("subclass terminated");
   // this.recordSender.terminate();
    
    super.stop();
  }
}
