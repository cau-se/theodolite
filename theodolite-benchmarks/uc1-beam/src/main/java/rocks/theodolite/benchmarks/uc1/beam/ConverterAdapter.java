package rocks.theodolite.benchmarks.uc1.beam;

import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.TypeDescriptor;
import rocks.theodolite.benchmarks.uc1.commons.RecordConverter;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * {@link SimpleFunction} which wraps a {@link RecordConverter} to be used with Beam.
 *
 * @param <T> type the {@link RecordConverter} is associated with.
 */
public class ConverterAdapter<T> extends SimpleFunction<ActivePowerRecord, T> {

  private static final long serialVersionUID = -5263671231838353747L; // NOPMD

  private final RecordConverter<T> converter;
  private final TypeDescriptor<T> type;

  /**
   * Create a new {@link ConverterAdapter} with a given {@link RecordConverter} and the associated
   * type.
   */
  public ConverterAdapter(final RecordConverter<T> converter, final Class<T> type) {
    this(converter, TypeDescriptor.of(type));
  }

  /**
   * Create a new {@link ConverterAdapter} with a given {@link RecordConverter} and the associated
   * type.
   */
  public ConverterAdapter(final RecordConverter<T> recordConverter, final TypeDescriptor<T> type) {
    super();
    this.converter = recordConverter;
    this.type = type;
  }

  @Override
  public T apply(final ActivePowerRecord record) {
    return this.converter.convert(record);
  }

  @Override
  public TypeDescriptor<T> getOutputTypeDescriptor() {
    return this.type;
  }

}
