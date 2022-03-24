package rocks.theodolite.benchmarks.uc3.beam.pubsub;

import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO.PubsubTopic;

/**
 * Factory methods for creating {@link PubsubTopic}s.
 */
public final class PubSubTopicFactory {

  private PubSubTopicFactory() {}

  /**
   * Create a {@link PubsubTopic} for the given project ID and topic ID.
   */
  public static PubsubTopic create(final String projectId, final String topicId) {
    return PubsubTopic.fromPath("projects/" + projectId + "/topics/" + topicId);
  }

}
