package theodolite.commons.flink.serialization;

import java.io.Serializable;
import java.util.function.Supplier;

public interface SerializableSupplier<T> extends Supplier<T>, Serializable {
  // here be dragons
}
