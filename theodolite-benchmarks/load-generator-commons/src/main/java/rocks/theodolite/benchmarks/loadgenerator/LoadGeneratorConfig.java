package rocks.theodolite.benchmarks.loadgenerator;

/**
 * Configuration of a load generator.
 */
public class LoadGeneratorConfig {

  private final GeneratorAction messageGenerator;
  private BeforeAction beforeAction = BeforeAction.doNothing();
  private int threads = 1;

  public <T> LoadGeneratorConfig(
      final RecordGenerator<? extends T> generator,
      final RecordSender<? super T> sender) {
    this.messageGenerator = GeneratorAction.from(generator, sender);
  }

  public <T> LoadGeneratorConfig(
      final RecordGenerator<? extends T> generator,
      final RecordSender<? super T> sender,
      final int threads) {
    this(generator, sender);
    this.threads = threads;
  }

  public void setThreads(final int threads) {
    this.threads = threads;
  }

  public void setBeforeAction(final BeforeAction beforeAction) {
    this.beforeAction = beforeAction;
  }

  public BeforeAction getBeforeAction() {
    return this.beforeAction;
  }

  public LoadGeneratorExecution buildLoadGeneratorExecution(
      final WorkloadDefinition workloadDefinition) {
    return new LoadGeneratorExecution(workloadDefinition, this.messageGenerator, this.threads);
  }

}
