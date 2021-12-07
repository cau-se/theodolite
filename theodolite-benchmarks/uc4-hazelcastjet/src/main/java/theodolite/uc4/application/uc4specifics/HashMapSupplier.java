package theodolite.uc4.application.uc4specifics;

import com.hazelcast.function.SupplierEx;
import java.util.HashMap;
import java.util.Set;

public class HashMapSupplier implements SupplierEx<HashMap<String,Set<String>>>{
  
  private static final long serialVersionUID = -6247504592403610702L;

  @Override
  public HashMap<String, Set<String>> get() {
    return new HashMap<String, Set<String>>();
  }

  @Override
  public HashMap<String, Set<String>> getEx() throws Exception {
    return null;
  }

  
  
}
