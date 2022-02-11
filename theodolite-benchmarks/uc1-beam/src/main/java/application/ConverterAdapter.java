package application;

import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.TypeDescriptor;
import rocks.theodolite.benchmarks.uc1.commons.RecordConverter;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * {@link SimpleFunction} which wraps a {@link RecordConverter} to be used with Beam.
 */
public class ConverterAdapter<T> extends SimpleFunction<ActivePowerRecord, T> {

  private static final long serialVersionUID = -5263671231838353747L; // NOPMD

  private final RecordConverter<T> recordConverter;
  private final TypeDescriptor<T> type;

  public ConverterAdapter(final RecordConverter<T> recordConverter, Class<T> type) {
    this.recordConverter = recordConverter;
    this.type = TypeDescriptor.of(type);
  }

  @Override
  public T apply(final ActivePowerRecord record) {
    return this.recordConverter.convert(record);
  }

  @Override
  public TypeDescriptor<T> getOutputTypeDescriptor() {
    return this.type;
  }

}
