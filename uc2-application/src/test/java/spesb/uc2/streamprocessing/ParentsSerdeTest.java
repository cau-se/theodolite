package spesb.uc2.streamprocessing;

import java.util.Set;
import org.junit.Test;
import spesb.uc2.streamprocessing.ParentsSerde;

public class ParentsSerdeTest {

  private final SerdeTesterFactory<Set<String>> serdeTesterFactory =
      new SerdeTesterFactory<>(ParentsSerde.serde());

  @Test
  public void test() {
    final Set<String> parents = Set.of("parent1", "parent2", "parent3");
    final SerdeTester<Set<String>> tester = this.serdeTesterFactory.create(parents);
    tester.test(o -> o);
  }

}
