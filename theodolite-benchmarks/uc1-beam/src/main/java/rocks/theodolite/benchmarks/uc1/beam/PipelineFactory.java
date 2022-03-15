package rocks.theodolite.benchmarks.uc1.beam;

import java.util.function.Function;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO.PubsubTopic;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO.Read;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.configuration2.Configuration;
import rocks.theodolite.benchmarks.commons.beam.AbstractPipelineFactory;
import rocks.theodolite.benchmarks.commons.beam.kafka.KafkaActivePowerTimestampReader;
import rocks.theodolite.benchmarks.uc1.beam.firestore.FirestoreOptionsExpander;
import rocks.theodolite.benchmarks.uc1.beam.pubsub.PubSubEncoder;
import rocks.theodolite.benchmarks.uc1.beam.pubsub.PubSubTopicFactory;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * {@link AbstractPipelineFactory} for UC1.
 */
public class PipelineFactory extends AbstractPipelineFactory {

  public static final String SOURCE_TYPE_KEY = "source.type";
  public static final String SINK_TYPE_KEY = "sink.type";

  private final SinkType sinkType = SinkType.from(this.config.getString(SINK_TYPE_KEY));

  public PipelineFactory(final Configuration configuration) {
    super(configuration);
  }

  @Override
  protected void expandOptions(final PipelineOptions options) {
    // No options to set
    // TODO Add for PubSub
    // final String pubSubEmulatorHost = super.config.getString(null);
    // if (pubSubEmulatorHost != null) {
    // final PubsubOptions pubSubOptions = options.as(PubsubOptions.class);
    // pubSubOptions.setPubsubRootUrl("http://" + pubSubEmulatorHost);
    // }
    if (this.sinkType == SinkType.FIRESTORE) {
      FirestoreOptionsExpander.expandOptions(options);
    }
  }

  @Override
  protected void constructPipeline(final Pipeline pipeline) {
    final SinkType sinkType = SinkType.from(this.config.getString(SINK_TYPE_KEY));
    final String sourceType = this.config.getString(SOURCE_TYPE_KEY);

    PCollection<ActivePowerRecord> activePowerRecords;

    if (sourceType.equals("pubsub")) {
      final String topicName = "input";
      final String projectName = "dummy-project-id";
      final PubsubTopic topic = PubSubTopicFactory.create(projectName, topicName);
      final Read<PubsubMessage> pubsub = PubsubIO
          .readMessages()
          .fromTopic(topic.asPath());

      // Read messages from Pub/Sub and encode them as Avro records
      activePowerRecords = pipeline.apply(pubsub).apply(MapElements.via(new PubSubEncoder()));
    } else {
      final KafkaActivePowerTimestampReader kafka = super.buildKafkaReader();
      // Read messages from Kafka as Avro records and drop keys
      activePowerRecords = pipeline.apply(kafka).apply(Values.create());
    }

    // Forward Avro records to configured sink
    activePowerRecords.apply(sinkType.create(this.config));
  }

  @Override
  protected void registerCoders(final CoderRegistry registry) {
    registry.registerCoderForClass(
        ActivePowerRecord.class,
        AvroCoder.of(ActivePowerRecord.class, false));
  }

  public static Function<Configuration, AbstractPipelineFactory> factory() {
    return config -> new PipelineFactory(config);
  }

}
