package rocks.theodolite.benchmarks.uc1.beam;

import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseAdapter;

/**
 * A {@link PTransform} for a generic {@link DatabaseAdapter}.
 *
 * @param <T> Type parameter of {@link DatabaseAdapter}.
 */
public class GenericSink<T> extends PTransform<PCollection<ActivePowerRecord>, PCollection<?>> {

  private static final long serialVersionUID = 1L;

  private final DatabaseAdapter<T> databaseAdapter;
  private final Class<T> type;

  /**
   * Create a {@link GenericSink} for the provided {@link DatabaseAdapter}. Requires also the
   * corresponding {@link Class} object for Beam.
   */
  public GenericSink(final DatabaseAdapter<T> databaseAdapter, final Class<T> type) {
    super();
    this.databaseAdapter = databaseAdapter;
    this.type = type;
  }

  @Override
  public PCollection<?> expand(final PCollection<ActivePowerRecord> activePowerRecords) {
    return activePowerRecords
        .apply(MapElements
            .via(new ConverterAdapter<>(this.databaseAdapter.getRecordConverter(), this.type)))
        .apply(ParDo.of(new WriterAdapter<>(this.databaseAdapter.getDatabaseWriter())));

  }

}
