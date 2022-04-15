package rocks.theodolite.benchmarks.uc1.beam;

import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.TypeDescriptor;
import rocks.theodolite.benchmarks.uc1.commons.RecordConverter;
import rocks.theodolite.commons.model.records.ActivePowerRecord;

/**
 * {@link SimpleFunction} which wraps a {@link RecordConverter} to be used with Beam.
 * 
 * @param <T> type the {@link RecordConverter} is associated with.
 */
public class ConverterAdapter<T> extends SimpleFunction<ActivePowerRecord, T> {

  private static final long serialVersionUID = -5263671231838353747L; // NOPMD

  private final RecordConverter<T> recordConverter;
  private final TypeDescriptor<T> type;

  /**
   * Create a new {@link ConverterAdapter} with a given {@link RecordConverter} and the associated
   * type.
   */
  public ConverterAdapter(final RecordConverter<T> recordConverter, final Class<T> type) {
    super();
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
