package application;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.Values;
import org.apache.commons.configuration2.Configuration;
import theodolite.commons.beam.AbstractPipeline;
import theodolite.commons.beam.kafka.KafkaActivePowerTimestampReader;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Implementation of benchmark UC1: Database Storage with Apache Beam.
 */
public final class Uc1BeamPipeline extends AbstractPipeline {

  public static final String SINK_TYPE_KEY = "sink.type";

  protected Uc1BeamPipeline(final PipelineOptions options, final Configuration config) {
    super(options, config);

    final SinkType sinkType = SinkType.from(config.getString(SINK_TYPE_KEY));

    // Set Coders for classes that will be distributed
    final CoderRegistry cr = super.getCoderRegistry();
    cr.registerCoderForClass(ActivePowerRecord.class, AvroCoder.of(ActivePowerRecord.SCHEMA$));

    final KafkaActivePowerTimestampReader kafka = new KafkaActivePowerTimestampReader(
        super.bootstrapServer,
        super.inputTopic,
        super.buildConsumerConfig());

    super.apply(kafka)
        .apply(Values.create())
        .apply(sinkType.create(config));
  }

}

