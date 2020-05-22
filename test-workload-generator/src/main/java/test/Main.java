package test;

import common.dimensions.Duration;
import common.dimensions.KeySpace;
import common.dimensions.Period;
import common.generators.KafkaWorkloadGenerator;
import common.generators.KafkaWorkloadGeneratorBuilder;
import common.messages.OutputMessage;
import common.misc.ZooKeeper;
import communication.kafka.KafkaRecordSender;
import java.util.concurrent.TimeUnit;
import kieker.common.record.IMonitoringRecord;
import titan.ccp.models.records.ActivePowerRecord;

public class Main {

  public static void main(final String[] args) {

    final KafkaRecordSender<IMonitoringRecord> recordSender =
        new KafkaRecordSender<>("localhost:9092", "input");

    final KafkaWorkloadGenerator<IMonitoringRecord> generator =
        KafkaWorkloadGeneratorBuilder.builder()
            .setZooKeeper(new ZooKeeper("127.0.0.1", 2181))
            .setKafkaRecordSender(recordSender)
            .setBeforeAction(() -> {
              System.out.println("Before Hook");
            })
            .setKeySpace(new KeySpace(5))
            .setPeriod(new Period(1000, TimeUnit.MILLISECONDS))
            .setDuration(new Duration(60, TimeUnit.SECONDS))
            .setGeneratorFunction(
                key -> new OutputMessage<>(key,
                    new ActivePowerRecord(key, 0L, 100d)))
            .build();

    generator.start();
  }

}
