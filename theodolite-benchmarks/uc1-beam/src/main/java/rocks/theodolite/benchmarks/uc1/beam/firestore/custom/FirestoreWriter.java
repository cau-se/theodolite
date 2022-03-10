package rocks.theodolite.benchmarks.uc1.beam.firestore.custom;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.spotify.futures.ApiFuturesExtra;
import com.spotify.scio.transforms.JavaAsyncDoFn;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.beam.sdk.transforms.DoFn;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Writes string records to a Firestore.
 */
public class FirestoreWriter extends JavaAsyncDoFn<ActivePowerRecord, String, Void> {

  private static final long serialVersionUID = -5263671231838353749L; // NOPMD

  // private static final Logger LOGGER = LoggerFactory.getLogger(FirestoreWriter.class);

  private final String collectionName;

  private transient Firestore firestore;

  public FirestoreWriter(final String collectionName) {
    super();
    this.collectionName = collectionName;
  }

  @Override
  public CompletableFuture<String> processElement(final ActivePowerRecord record) {
    this.initFirestore();

    final Map<String, Object> map = Map.of(
        "identifier", record.getIdentifier(),
        "timestamp", record.getTimestamp(),
        "valueInW", record.getValueInW());

    return ApiFuturesExtra.toCompletableFuture(
        this.firestore.collection(this.collectionName).add(map))
        .thenApply(DocumentReference::getPath);
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.PER_CLASS;
  }

  @Override
  public Void createResource() {
    return null;
  }

  private void initFirestore() {
    if (this.firestore == null) {
      try {
        final FirestoreOptions firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
            .setCredentials(GoogleCredentials.getApplicationDefault())
            .build();
        this.firestore = firestoreOptions.getService();
      } catch (final IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  /**
   * Return this {@link FirestoreWriter} as {@code DoFn<Map<String, Object>, DocumentReference> }.
   * Used to circumvent a bug in Eclipse.
   */
  public DoFn<ActivePowerRecord, String> asDoFn() {
    return this;
  }

}
