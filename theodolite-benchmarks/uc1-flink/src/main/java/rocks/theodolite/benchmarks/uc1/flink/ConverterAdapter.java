package rocks.theodolite.benchmarks.uc1.flink;

import org.apache.flink.api.common.functions.MapFunction;
import rocks.theodolite.benchmarks.uc1.commons.RecordConverter;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * {@link MapFunction} which wraps a {@link RecordConverter} to be used with Flink.
 *
 * @param <T> type the {@link RecordConverter} is associated with.
 */
public class ConverterAdapter<T> implements MapFunction<ActivePowerRecord, T> {

  private static final long serialVersionUID = -5263671231838353747L; // NOPMD

  private final RecordConverter<T> recordConverter;

  public ConverterAdapter(final RecordConverter<T> recordConverter) {
    this.recordConverter = recordConverter;
  }

  @Override
  public T map(final ActivePowerRecord record) throws Exception {
    return this.recordConverter.convert(record);
  }

}
