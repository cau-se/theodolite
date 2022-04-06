package rocks.theodolite.benchmarks.uc1.beam.dynamodb;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.model.PutRequest;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import org.apache.beam.sdk.io.aws.dynamodb.DynamoDBIO;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.configuration2.Configuration;
import org.joda.time.Duration;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * A {@link PTransform} mapping {@link ActivePowerRecord}s to {@link PutRequest}s, followed by
 * storing these them to AWS DynamoDB.
 */
public class DynamoDbSink extends PTransform<PCollection<ActivePowerRecord>, PCollection<?>> {

  public static final String SINK_DYNAMODB_TABLE_KEY = "sink.dynamodb.table";

  private static final String SINK_DYNAMODB_REGION_KEY = "sink.dynamodb.region";

  private static final String SINK_AWS_ACCESS_KEY_KEY = "sink.dynamodb.aws.access.key";

  private static final String SINK_AWS_SECRET_KEY_KEY = "sink.dynamodb.aws.secret.key";

  private static final int MAX_RETRIES = 5;

  private static final long serialVersionUID = 1L;

  // private static final Logger LOGGER = LoggerFactory.getLogger(FirestoreSink.class);

  private final String tableName;
  private final String awsAccessKey;
  private final String awsSecretKey;
  private final Regions regions;


  /**
   * Create a new {@link DynamoDbSink} based on the provided table name, AWS credentials and
   * provided region..
   */
  public DynamoDbSink(final String tableName, final String awsAccessKey, final String awsSecretKey,
      final Regions regions) {
    super();
    this.tableName = tableName;
    this.awsAccessKey = awsAccessKey;
    this.awsSecretKey = awsSecretKey;
    this.regions = regions;
  }

  @Override
  public PCollection<?> expand(final PCollection<ActivePowerRecord> activePowerRecords) {


    return activePowerRecords
        .apply(MapElements.via(new PutRequestMapper()))
        .apply(DynamoDBIO.<WriteRequest>write()
            .withWriteRequestMapperFn(
                (SerializableFunction<WriteRequest, KV<String, WriteRequest>>) r -> KV
                    .of(this.tableName, r))
            .withRetryConfiguration(
                DynamoDBIO.RetryConfiguration.create(MAX_RETRIES, Duration.standardMinutes(1)))
            .withAwsClientsProvider(this.awsAccessKey, this.awsSecretKey, this.regions));
  }

  /**
   * Create a {@link DynamoDbSink} from the provided {@link Configuration} object.
   */
  public static DynamoDbSink fromConfig(final Configuration config) {
    final String tableName = config.getString(SINK_DYNAMODB_TABLE_KEY);
    final String awsAccessKey = config.getString(SINK_AWS_ACCESS_KEY_KEY);
    final String awsSecretKey = config.getString(SINK_AWS_SECRET_KEY_KEY);
    final Regions regions = Regions.fromName(config.getString(SINK_DYNAMODB_REGION_KEY));
    return new DynamoDbSink(
        tableName,
        awsAccessKey,
        awsSecretKey,
        regions);
  }
}
