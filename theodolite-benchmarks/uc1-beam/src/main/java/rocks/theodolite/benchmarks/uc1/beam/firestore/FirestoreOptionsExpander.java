package rocks.theodolite.benchmarks.uc1.beam.firestore;

import java.io.IOException;
import org.apache.beam.sdk.extensions.gcp.options.GcpOptions;
import org.apache.beam.sdk.options.PipelineOptions;

/**
 * Provides a method to expand {@link PipelineOptions} for Firestore.
 */
public final class FirestoreOptionsExpander {

  private FirestoreOptionsExpander() {}

  /**
   * Expand {@link PipelineOptions} by special options required for Firestore derived from a default
   * configuration.
   *
   * @param options {@link PipelineOptions} to be expanded.
   */
  public static void expandOptions(final PipelineOptions options) {
    final GcpOptions firestoreOptions = options.as(GcpOptions.class);
    final FirestoreConfig firestoreConfig = getFirestoreConfig();
    firestoreOptions.setProject(firestoreConfig.getProjectId());
  }

  private static FirestoreConfig getFirestoreConfig() {
    try {
      return FirestoreConfig.createFromDefaults();
    } catch (final IOException e) {
      throw new IllegalStateException("Cannot create Firestore configuration.", e);
    }
  }

}
