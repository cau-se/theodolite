package theodolite.commons.workloadgeneration.functions;

/**
 * Interface representing a message generator, which sends messages for given keys to some
 * destination.
 */
@FunctionalInterface
public interface MessageGenerator {

  void generate(final String key);

  public static <T> MessageGenerator from(
      final RecordGenerator<T> generator,
      final RecordSender<T> sender) {
    return key -> sender.send(generator.generate(key));
  }

}
