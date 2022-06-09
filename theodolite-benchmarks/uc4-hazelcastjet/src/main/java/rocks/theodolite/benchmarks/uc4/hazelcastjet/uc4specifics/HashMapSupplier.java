package rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics;

import com.hazelcast.function.SupplierEx;
import java.util.HashMap;
import java.util.Set;

/**
 * Supplies a {@link HashMap} and implements {@link SupplierEx}.
 */
public class HashMapSupplier implements SupplierEx<HashMap<String, Set<String>>> {

  private static final long serialVersionUID = -6247504592403610702L; // NOPMD

  @Override
  public HashMap<String, Set<String>> get() {
    return new HashMap<>();
  }

  @Override
  public HashMap<String, Set<String>> getEx() throws Exception {
    return this.get();
  }



}
