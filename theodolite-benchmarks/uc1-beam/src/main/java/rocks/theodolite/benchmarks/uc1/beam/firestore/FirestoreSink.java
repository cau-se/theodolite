package rocks.theodolite.benchmarks.uc1.beam.firestore;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firestore.v1.Document;
import org.apache.beam.sdk.io.gcp.firestore.FirestoreIO;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.configuration2.Configuration;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;

/**
 * A {@link PTransform} mapping {@link ActivePowerRecord}s to {@link Document}s, followed by storing
 * these {@link DocumentSnapshot} to Firestore.
 */
public class FirestoreSink extends PTransform<PCollection<ActivePowerRecord>, PCollection<?>> {

  public static final String SINK_FIRESTORE_COLLECTION_KEY = "sink.firestore.collection";

  private static final long serialVersionUID = 1L;

  private final String collectionName;

  public FirestoreSink(final String collectionName) {
    super();
    this.collectionName = collectionName;
  }

  @Override
  public PCollection<?> expand(final PCollection<ActivePowerRecord> activePowerRecords) {
    return activePowerRecords
        .apply(MapElements.via(new DocumentMapper(this.collectionName)))
        .apply(MapElements.via(new UpdateOperationMapper()))
        .apply(FirestoreIO.v1().write().batchWrite().build());
  }

  public static FirestoreSink fromConfig(final Configuration config) {
    final String collectionName = config.getString(SINK_FIRESTORE_COLLECTION_KEY);
    return new FirestoreSink(collectionName);
  }
}
