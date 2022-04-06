package rocks.theodolite.benchmarks.uc1.beam;

import java.util.stream.Stream;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.configuration2.Configuration;
import rocks.theodolite.benchmarks.uc1.beam.dynamodb.DynamoDbSink;
import rocks.theodolite.benchmarks.uc1.beam.firestore.FirestoreSink;
import rocks.theodolite.benchmarks.uc1.beam.firestore.custom.CustomFirestoreSink;
import rocks.theodolite.benchmarks.uc1.commons.logger.LogWriterFactory;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Supported Sink types, i.e., {@link PTransform} for converting and storing
 * {@link ActivePowerRecord}s.
 */
public enum SinkType implements SinkFactory {

  LOGGER("logger") {
    @Override
    public PTransform<PCollection<ActivePowerRecord>, PCollection<?>> create(
        final Configuration config) {
      return new GenericSink<>(LogWriterFactory.forJson(), String.class);
    }
  },
  FIRESTORE("firestore") {
    @Override
    public PTransform<PCollection<ActivePowerRecord>, PCollection<?>> create(
        final Configuration config) {
      return FirestoreSink.fromConfig(config);
    }
  },
  FIRESTORE_CUSTOM("firestoreCustom") {
    @Override
    public PTransform<PCollection<ActivePowerRecord>, PCollection<?>> create(
        final Configuration config) {
      return CustomFirestoreSink.fromConfig(config);
    }
  },
  DYNAMO_DB("dynamodb") {
    @Override
    public PTransform<PCollection<ActivePowerRecord>, PCollection<?>> create(
        final Configuration config) {
      return DynamoDbSink.fromConfig(config);
    }
  };

  private final String value;

  SinkType(final String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }

  /**
   * Create a new {@link SinkType} from its string representation.
   */
  public static SinkType from(final String value) {
    return Stream.of(SinkType.values())
        .filter(t -> t.value.equals(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Sink '" + value + "' does not exist."));
  }

}
