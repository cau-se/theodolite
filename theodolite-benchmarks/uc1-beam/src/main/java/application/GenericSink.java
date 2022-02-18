package application;

import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseAdapter;
import titan.ccp.model.records.ActivePowerRecord;

public class GenericSink<T> extends PTransform<PCollection<ActivePowerRecord>, PCollection<?>> {

  private static final long serialVersionUID = 1L;

  private final DatabaseAdapter<T> databaseAdapter;
  private final Class<T> type;

  public GenericSink(DatabaseAdapter<T> databaseAdapter, Class<T> type) {
    this.databaseAdapter = databaseAdapter;
    this.type = type;
  }

  @Override
  public PCollection<?> expand(PCollection<ActivePowerRecord> activePowerRecords) {
    return activePowerRecords
        .apply(MapElements
            .via(new ConverterAdapter<>(this.databaseAdapter.getRecordConverter(), this.type)))
        .apply(ParDo.of(new WriterAdapter<>(this.databaseAdapter.getDatabaseWriter())));

  }

}
