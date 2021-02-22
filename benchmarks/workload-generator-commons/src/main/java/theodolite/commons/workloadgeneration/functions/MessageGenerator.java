package theodolite.commons.workloadgeneration.functions;

@FunctionalInterface
public interface MessageGenerator {

  void generate(final String key);

  public static <T> MessageGenerator from(
      final RecordGenerator<T> generator,
      final RecordSender<T> sender) {
    return key -> sender.send(generator.generate(key));
  }

}
