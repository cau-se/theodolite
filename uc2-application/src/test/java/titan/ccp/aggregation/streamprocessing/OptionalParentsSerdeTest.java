package titan.ccp.aggregation.streamprocessing;

import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import titan.ccp.aggregation.streamprocessing.OptionalParentsSerde;

public class OptionalParentsSerdeTest {

  private final SerdeTesterFactory<Optional<Set<String>>> serdeTesterFactory =
      new SerdeTesterFactory<>(OptionalParentsSerde.serde());

  @Test
  public void testPresent() {
    final Set<String> parents = Set.of("parent1", "parent2", "parent3");
    final Optional<Set<String>> optionalParents = Optional.of(parents);
    final SerdeTester<Optional<Set<String>>> tester =
        this.serdeTesterFactory.create(optionalParents);
    tester.test();
  }

  @Test
  public void testAbsent() {
    final Optional<Set<String>> optionalParents = Optional.empty();
    final SerdeTester<Optional<Set<String>>> tester =
        this.serdeTesterFactory.create(optionalParents);
    tester.test();
  }

}
