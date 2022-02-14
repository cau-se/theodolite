package application;

import org.apache.beam.sdk.transforms.DoFn;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseAdapter;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseWriter;

/**
 * {@link DoFn} which wraps a {@link DatabaseAdapter} to be used with Beam.
 */
public class WriterAdapter<T> extends DoFn<T, Void> {

  private static final long serialVersionUID = -5263671231838353742L; // NOPMD

  private final DatabaseWriter<T> databaseWriter;

  public WriterAdapter(final DatabaseWriter<T> databaseWriter) {
    super();
    this.databaseWriter = databaseWriter;
  }

  @ProcessElement
  public void processElement(@Element final T record, final OutputReceiver<Void> out) {
    this.databaseWriter.write(record);
  }

}
