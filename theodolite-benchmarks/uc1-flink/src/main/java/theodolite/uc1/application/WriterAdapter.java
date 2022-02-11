package theodolite.uc1.application;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseAdapter;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseWriter;

/**
 * {@link FlatMapFunction} which wraps a {@link DatabaseAdapter} to be used with Flink.
 */
public class WriterAdapter<T> implements FlatMapFunction<T, Void> {

  private static final long serialVersionUID = -5263671231838353747L; // NOPMD

  private final DatabaseWriter<T> databaseWriter;

  public WriterAdapter(final DatabaseWriter<T> databaseWriter) {
    this.databaseWriter = databaseWriter;
  }

  @Override
  public void flatMap(final T value, final Collector<Void> out) throws Exception {
    this.databaseWriter.write(value);
  }

}
