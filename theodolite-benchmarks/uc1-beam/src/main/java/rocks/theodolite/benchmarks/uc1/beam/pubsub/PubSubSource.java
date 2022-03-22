package rocks.theodolite.benchmarks.uc1.beam.pubsub;

import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO.Read;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * {@link PTransform} reading {@link ActivePowerRecord}s from Pub/Sub.
 */
public final class PubSubSource extends PTransform<PBegin, PCollection<ActivePowerRecord>> {

  private static final long serialVersionUID = 2603286151183186115L;

  private final Read<PubsubMessage> pubsubRead;

  private PubSubSource(final Read<PubsubMessage> pubsubRead) {
    super();
    this.pubsubRead = pubsubRead;
  }

  @Override
  public PCollection<ActivePowerRecord> expand(final PBegin input) {
    // Read messages from Pub/Sub and encode them as Avro records
    return input.apply(this.pubsubRead).apply(MapElements.via(new PubSubEncoder()));
  }

  /**
   * Create a new {@link PubSubSource} for the given project and topic.
   */
  public static final PubSubSource forTopic(final String projectName, final String topicName) {
    return new PubSubSource(PubsubIO
        .readMessages()
        .fromTopic(PubSubTopicFactory.create(projectName, topicName).asPath()));
  }

  /**
   * Create a new {@link PubSubSource} for the given project and subscription.
   */
  public static final PubSubSource forSubscription(final String projectName,
      final String subscriptionName) {
    return new PubSubSource(PubsubIO
        .readMessages()
        .fromSubscription(
            PubSubSubscriptionFactory.create(projectName, subscriptionName).asPath()));
  }

}
