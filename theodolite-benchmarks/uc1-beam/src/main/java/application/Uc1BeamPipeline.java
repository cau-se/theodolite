package application;

import application.pubsub.PubSubEncoder;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO.Read;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.configuration2.Configuration;
import theodolite.commons.beam.AbstractPipeline;
import theodolite.commons.beam.kafka.KafkaActivePowerTimestampReader;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Implementation of benchmark UC1: Database Storage with Apache Beam.
 */
public final class Uc1BeamPipeline extends AbstractPipeline {

  public static final String SINK_TYPE_KEY = "sink.type";
  public static final String SOURCE_TYPE_KEY = "source.type";

  protected Uc1BeamPipeline(final PipelineOptions options, final Configuration config) {
    super(options, config);

    // Set Coders for classes that will be distributed
    final CoderRegistry cr = super.getCoderRegistry();
    cr.registerCoderForClass(ActivePowerRecord.class,
        AvroCoder.of(ActivePowerRecord.class, false));

    final SinkType sinkType = SinkType.from(config.getString(SINK_TYPE_KEY));

    final String sourceType = config.getString(SOURCE_TYPE_KEY);

    PCollection<ActivePowerRecord> activePowerRecords;

    if (sourceType == "pubsub") {
      final String topic = "input";
      final String project = "dummy-project-id";
      final Read<PubsubMessage> pubsub = PubsubIO
          .readMessages()
          .fromTopic("projects/" + project + "/topics/" + topic);

      activePowerRecords = super.apply(pubsub).apply(MapElements.via(new PubSubEncoder()));
    } else {
      final KafkaActivePowerTimestampReader kafka = new KafkaActivePowerTimestampReader(
          super.bootstrapServer,
          super.inputTopic,
          super.buildConsumerConfig());

      activePowerRecords = super.apply(kafka).apply(Values.create());
    }

    activePowerRecords.apply(sinkType.create(config));
  }

}

