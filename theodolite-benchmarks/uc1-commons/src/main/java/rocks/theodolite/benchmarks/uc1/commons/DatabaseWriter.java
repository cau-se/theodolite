package rocks.theodolite.benchmarks.uc1.commons;

/**
 * Writes an object to a database.
 *
 * @param <T> Type expected by the database.
 */
@FunctionalInterface
public interface DatabaseWriter<T> {

  void write(T record);

}
