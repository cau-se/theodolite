package theodolite.commons.flink;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;

/**
 * Helper methods for creating {@link TypeInformation} for {@link Tuple}s. In contrast to
 * {@code Types#TUPLE(TypeInformation...)}, these methods bring real type safety.
 */
public final class TupleType {

  private TupleType() {}

  public static <T1, T2> TypeInformation<Tuple2<T1, T2>> of(// NOPMD
      final TypeInformation<T1> t0,
      final TypeInformation<T2> t1) {
    return Types.TUPLE(t0, t1);
  }

}
