package rocks.theodolite.benchmarks.uc1.beam.firestore;

import com.google.firestore.v1.Document;
import com.google.firestore.v1.Value;
import java.io.IOException;
import org.apache.beam.sdk.transforms.SimpleFunction;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;

final class DocumentMapper extends SimpleFunction<ActivePowerRecord, Document> {

  private static final long serialVersionUID = -5263671231838353749L; // NOPMD

  private transient FirestoreConfig firestoreConfig;

  private final String collection;

  public DocumentMapper(final String collection) {
    super();
    this.collection = collection;
  }

  @Override
  public Document apply(final ActivePowerRecord record) {
    return Document
        .newBuilder()
        .setName(this.createDocumentName(record.getIdentifier() + record.getTimestamp()))
        .putFields("identifier",
            Value.newBuilder().setStringValue(record.getIdentifier()).build())
        .putFields("timestamp", Value.newBuilder().setIntegerValue(record.getTimestamp()).build())
        .putFields("valueInW", Value.newBuilder().setDoubleValue(record.getValueInW()).build())
        .build();
  }

  private String createDocumentName(final String documentId) {
    this.initFirestoreConfig();
    return "projects/" + this.firestoreConfig.getProjectId()
        + "/databases/" + this.firestoreConfig.getDatabase()
        + "/documents/" + this.collection
        + "/" + documentId;
  }

  private void initFirestoreConfig() {
    if (this.firestoreConfig == null) {
      try {
        this.firestoreConfig = FirestoreConfig.createFromDefaults();
      } catch (final IOException e) {
        throw new IllegalStateException("Cannot create Firestore configuration.", e);
      }
    }
  }

}
