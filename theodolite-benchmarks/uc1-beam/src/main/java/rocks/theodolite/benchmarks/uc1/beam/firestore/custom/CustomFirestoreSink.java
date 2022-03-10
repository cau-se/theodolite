package rocks.theodolite.benchmarks.uc1.beam.firestore.custom;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firestore.v1.Document;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * A {@link PTransform} mapping {@link ActivePowerRecord}s to {@link Document}s, followed by storing
 * these {@link DocumentSnapshot} to Firestore.
 */
public class CustomFirestoreSink
    extends PTransform<PCollection<ActivePowerRecord>, PCollection<?>> {

  public static final String SINK_FIRESTORE_COLLECTION_KEY = "sink.firestore.collection";

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomFirestoreSink.class);

  private final String collectionName;


  /**
   * Create a new {@link CustomFirestoreSink} based on the provided collection name and hint for the
   * max. number of workers.
   */
  public CustomFirestoreSink(final String collectionName) {
    super();
    this.collectionName = collectionName;
  }

  @Override
  public PCollection<?> expand(final PCollection<ActivePowerRecord> activePowerRecords) {
    return activePowerRecords
        // .apply(MapElements.via(new ConverterAdapter<>(new MapConverter(),
        // TypeDescriptors.maps(TypeDescriptors.strings(), TypeDescriptor.of(Object.class)))))
        .apply(MapElements.via(
            new SimpleFunction<ActivePowerRecord, ActivePowerRecord>() {

              private static final long serialVersionUID = 1L;

              @Override
              public ActivePowerRecord apply(final ActivePowerRecord record) {
                final long time = System.currentTimeMillis();
                LOGGER.info("[{}] Initiate write record {}", time, record);
                return record;
              }
            }))
        .apply(ParDo.of(new FirestoreWriter(this.collectionName)))
        .apply(MapElements.via(
            new SimpleFunction<String, String>() {

              private static final long serialVersionUID = 1L;

              @Override
              public String apply(final String resultPath) {
                final long time = System.currentTimeMillis();
                LOGGER.info("[{}] Finished write to {}", time, resultPath);
                return resultPath;
              }
            }));
  }

  /**
   * Create a {@link CustomFirestoreSink} from the provided {@link Configuration} object.
   */
  public static CustomFirestoreSink fromConfig(final Configuration config) {
    final String collectionName = config.getString(SINK_FIRESTORE_COLLECTION_KEY);
    return new CustomFirestoreSink(collectionName);
  }

}
