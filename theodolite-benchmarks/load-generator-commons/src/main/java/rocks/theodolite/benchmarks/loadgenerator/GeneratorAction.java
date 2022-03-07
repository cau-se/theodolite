package rocks.theodolite.benchmarks.loadgenerator;

/**
 * Interface representing a record generator action consisting of generating a record and sending
 * it.
 */
@FunctionalInterface
public interface GeneratorAction {

  void generate(final String key);

  default void shutdown() {
    // Nothing to do per default
  }

  public static <T> GeneratorAction from(
      final RecordGenerator<? extends T> generator,
      final RecordSender<? super T> sender) {
    return new GeneratorActionImpl<>(generator, sender);
  }

}
