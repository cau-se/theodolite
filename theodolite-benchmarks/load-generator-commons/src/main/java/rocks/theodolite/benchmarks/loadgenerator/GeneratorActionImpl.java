package rocks.theodolite.benchmarks.loadgenerator;

class GeneratorActionImpl<T> implements GeneratorAction {

  private final RecordGenerator<? extends T> generator;

  private final RecordSender<? super T> sender;

  public GeneratorActionImpl(
      final RecordGenerator<? extends T> generator,
      final RecordSender<? super T> sender) {
    this.generator = generator;
    this.sender = sender;
  }

  @Override
  public void shutdown() {
    this.generator.close();
    this.sender.close();
  }

  @Override
  public void generate(final String key) {
    this.sender.send(this.generator.generate(key));
  }

}
