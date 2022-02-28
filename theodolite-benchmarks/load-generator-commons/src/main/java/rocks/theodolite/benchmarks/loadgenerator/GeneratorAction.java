package rocks.theodolite.benchmarks.loadgenerator;

/**
 * Interface representing a record generator action consisting of generating a record and sending
 * it.
 */
@FunctionalInterface
interface GeneratorAction {

  void generate(final String key);

  public static <T> GeneratorAction from(
      final RecordGenerator<? extends T> generator,
      final RecordSender<? super T> sender) {
    return key -> sender.send(generator.generate(key));
  }

}
