package test;

import common.KafkaWorkloadGenerator;
import common.KafkaWorkloadGeneratorBuilder;
import common.dimensions.Duration;
import common.dimensions.KeySpace;
import common.dimensions.Period;
import common.messages.OutputMessage;
import java.util.concurrent.TimeUnit;
import titan.ccp.models.records.ActivePowerRecord;

public class Main {

  public static void main(final String[] args) {

    final KafkaWorkloadGenerator generator =
        KafkaWorkloadGeneratorBuilder.builder()
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


    // dwhedhwedherbfherf ferufer e u uebvhebzvbjkr fjkebhr erfberf rt gtr grt gtr
    // gebuwbfuzerfuzerzgfer fe rf er fe rferhfveurfgerzfgzuerf erf erf ethvrif

    generator.start();
  }

}
