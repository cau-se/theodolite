package rocks.theodolite.benchmarks.uc1.beam.firestore;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.FirestoreOptions;
import java.io.IOException;

final class FirestoreConfig {

  private final FirestoreOptions firestoreOptions;

  private FirestoreConfig(final FirestoreOptions firestoreOptions) {
    this.firestoreOptions = firestoreOptions;
  }

  public String getProjectId() {
    return this.firestoreOptions.getProjectId();
  }

  public String getDatabase() {
    return this.firestoreOptions.getDatabaseId();
  }

  public static FirestoreConfig createFromDefaults() throws IOException {
    return new FirestoreConfig(FirestoreOptions.getDefaultInstance().toBuilder()
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build());
  }

}
