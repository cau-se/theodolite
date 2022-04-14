package rocks.theodolite.benchmarks.uc1.commons;

import java.util.Objects;
import rocks.theodolite.commons.model.records.ActivePowerRecord;

/**
 * A database adapter consisting of a {@link RecordConverter} and a {@link DatabaseWriter}.
 *
 * @param <T> intermediate data type written to the database.
 */
public final class DatabaseAdapter<T> {

  private final RecordConverter<T> recordConverter;

  private final DatabaseWriter<T> databaseWriter;

  private DatabaseAdapter(final RecordConverter<T> recordConverter,
      final DatabaseWriter<T> databaseWriter) {
    this.recordConverter = recordConverter;
    this.databaseWriter = databaseWriter;
  }

  public RecordConverter<T> getRecordConverter() {
    return this.recordConverter;
  }

  public DatabaseWriter<T> getDatabaseWriter() {
    return this.databaseWriter;
  }

  /**
   * Create a new {@link DatabaseAdapter}.
   *
   * @param <T> intermediate data type written to the database.
   * @param recordConverter RecordConverter for converting {@link ActivePowerRecord}s to {@code T}
   * @param databaseWriter DatabaseWriter for writing converted records to the database.
   * @return the {@link DatabaseAdapter}.
   */
  public static <T> DatabaseAdapter<T> from(final RecordConverter<T> recordConverter,
      final DatabaseWriter<T> databaseWriter) {
    Objects.requireNonNull(recordConverter);
    Objects.requireNonNull(databaseWriter);
    return new DatabaseAdapter<>(recordConverter, databaseWriter);
  }

}
