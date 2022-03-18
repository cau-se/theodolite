package rocks.theodolite.benchmarks.uc1.beam.firestore;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firestore.v1.Document;
import org.apache.beam.sdk.io.gcp.firestore.FirestoreIO;
import org.apache.beam.sdk.io.gcp.firestore.RpcQosOptions;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.configuration2.Configuration;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * A {@link PTransform} mapping {@link ActivePowerRecord}s to {@link Document}s, followed by storing
 * these {@link DocumentSnapshot} to Firestore.
 */
public class FirestoreSink extends PTransform<PCollection<ActivePowerRecord>, PCollection<?>> {

  public static final String SINK_FIRESTORE_COLLECTION_KEY = "sink.firestore.collection";

  public static final String SINK_FIRESTORE_MAX_WORKERS_KEY = "sink.firestore.hint.max.workers";

  private static final long serialVersionUID = 1L;

  // private static final Logger LOGGER = LoggerFactory.getLogger(FirestoreSink.class);

  private final String collectionName;

  private final RpcQosOptions rpcQosOptions;

  /**
   * Create a new {@link FirestoreSink} based on the provided collection name and hint for the max.
   * number of workers.
   */
  public FirestoreSink(final String collectionName, final int hintMaxNumWorkers) {
    super();
    this.collectionName = collectionName;
    this.rpcQosOptions = RpcQosOptions.newBuilder()
        .withHintMaxNumWorkers(hintMaxNumWorkers)
        .build();
  }

  @Override
  public PCollection<?> expand(final PCollection<ActivePowerRecord> activePowerRecords) {
    return activePowerRecords
        .apply(MapElements.via(new DocumentMapper(this.collectionName)))
        .apply(MapElements.via(new UpdateOperationMapper()))
        // .apply(MapElements.via(
        // new SimpleFunction<Write, Write>() {
        //
        // private static final long serialVersionUID = 1L;
        //
        // @Override
        // public Write apply(final Write write) {
        // final long time = System.currentTimeMillis();
        // final Map<String, Value> map = write.getUpdate().getFieldsMap();
        // LOGGER.info("[{}] Initiate write record {} {}", time,
        // map.get("identifier").getStringValue(),
        // map.get("timestamp").getIntegerValue());
        // return write;
        // }
        // }))
        .apply(FirestoreIO.v1().write().batchWrite().withRpcQosOptions(this.rpcQosOptions).build());
    // .apply(MapElements.via(
    // new SimpleFunction<WriteSuccessSummary, String>() {
    //
    // private static final long serialVersionUID = 1L;
    //
    // @Override
    // public String apply(final WriteSuccessSummary summary) {
    // final long time = System.currentTimeMillis();
    // LOGGER.info("[{}] Finished write with result {}", time, summary.toString());
    // return summary.toString();
    // }
    // }));
  }

  /**
   * Create a {@link FirestoreSink} from the provided {@link Configuration} object.
   */
  public static FirestoreSink fromConfig(final Configuration config) {
    final String collectionName = config.getString(SINK_FIRESTORE_COLLECTION_KEY);
    final int maxWorkers = config.getInt(SINK_FIRESTORE_MAX_WORKERS_KEY, 500);
    return new FirestoreSink(collectionName, maxWorkers);
  }
}
