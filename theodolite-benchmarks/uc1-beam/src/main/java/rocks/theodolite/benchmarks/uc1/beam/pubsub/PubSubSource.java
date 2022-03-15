package rocks.theodolite.benchmarks.uc1.beam.pubsub;

import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO.PubsubTopic;
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
public class PubSubSource extends PTransform<PBegin, PCollection<ActivePowerRecord>> {

  private static final long serialVersionUID = 2603286151183186115L;

  private final PubsubTopic topic;

  public PubSubSource(final String topicName, final String projectName) {
    super();
    this.topic = PubSubTopicFactory.create(projectName, topicName);
  }

  @Override
  public PCollection<ActivePowerRecord> expand(final PBegin input) {
    final Read<PubsubMessage> pubsub = PubsubIO
        .readMessages()
        .fromTopic(this.topic.asPath());

    // Read messages from Pub/Sub and encode them as Avro records
    return input.apply(pubsub).apply(MapElements.via(new PubSubEncoder()));
  }

}
