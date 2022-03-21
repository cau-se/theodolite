package rocks.theodolite.benchmarks.uc1.beam.pubsub;

import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO.PubsubSubscription;

/**
 * Factory methods for creating {@link PubsubSubscription}s.
 */
public final class PubSubSubscriptionFactory {

  private PubSubSubscriptionFactory() {}

  /**
   * Create a {@link PubsubSubscription} for the given project ID and subscription ID.
   */
  public static PubsubSubscription create(final String project, final String subscription) {
    return PubsubSubscription.fromPath("projects/" + project + "/subscriptions/" + subscription);
  }

}
