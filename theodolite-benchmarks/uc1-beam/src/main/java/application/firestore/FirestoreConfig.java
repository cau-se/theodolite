package application.firestore;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.FirestoreOptions;
import java.io.IOException;

final class FirestoreConfig {

  final FirestoreOptions firestoreOptions;

  private FirestoreConfig(FirestoreOptions firestoreOptions) {
    this.firestoreOptions = firestoreOptions;
  }

  public String getProjectId() {
    return this.firestoreOptions.getProjectId();
  }

  public String getDatabaseDdlRequest() {
    return this.firestoreOptions.getProjectId();
  }

  public static FirestoreConfig createFromDefaults() throws IOException {
    return new FirestoreConfig(FirestoreOptions.getDefaultInstance().toBuilder()
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build());
  }

}
