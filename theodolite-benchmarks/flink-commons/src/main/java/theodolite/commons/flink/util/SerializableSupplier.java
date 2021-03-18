package theodolite.commons.flink.util;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Interface for {@link Supplier}s which are serializable.
 *
 * @param <T> the type of results supplied by this supplier
 */
public interface SerializableSupplier<T> extends Supplier<T>, Serializable { // NOPMD
  // Nothing to do here
}
