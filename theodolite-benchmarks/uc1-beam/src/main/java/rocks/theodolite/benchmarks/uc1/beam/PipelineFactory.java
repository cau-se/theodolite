package rocks.theodolite.benchmarks.uc1.beam;

import java.util.function.Function;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.configuration2.Configuration;
import rocks.theodolite.benchmarks.commons.beam.AbstractPipelineFactory;
import rocks.theodolite.benchmarks.commons.beam.kafka.KafkaActivePowerTimestampReader;
import rocks.theodolite.benchmarks.uc1.beam.firestore.FirestoreOptionsExpander;
import rocks.theodolite.benchmarks.uc1.beam.pubsub.PubSubSource;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * {@link AbstractPipelineFactory} for UC1.
 */
public class PipelineFactory extends AbstractPipelineFactory {

  public static final String SOURCE_TYPE_KEY = "source.type";
  public static final String SINK_TYPE_KEY = "sink.type";

  public static final String PUBSSUB_SOURCE_PROJECT_KEY = "source.pubsub.project";
  public static final String PUBSSUB_SOURCE_TOPIC_KEY = "source.pubsub.topic";
  public static final String PUBSSUB_SOURCE_SUBSCR_KEY = "source.pubsub.subscription";

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

    if ("pubsub".equals(sourceType)) {
      final String project = this.config.getString(PUBSSUB_SOURCE_PROJECT_KEY);
      final String topic = this.config.getString(PUBSSUB_SOURCE_TOPIC_KEY);
      final String subscription = this.config.getString(PUBSSUB_SOURCE_SUBSCR_KEY);
      // Read messages from Pub/Sub and encode them as Avro records
      if (subscription == null) {
        activePowerRecords = pipeline.apply(PubSubSource.forTopic(topic, project));
      } else {
        activePowerRecords = pipeline.apply(PubSubSource.forSubscription(topic, subscription));
      }
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
        // AvroCoder.of(ActivePowerRecord.SCHEMA$));
        AvroCoder.of(ActivePowerRecord.class, false));
  }

  public static Function<Configuration, AbstractPipelineFactory> factory() {
    return config -> new PipelineFactory(config);
  }

}
