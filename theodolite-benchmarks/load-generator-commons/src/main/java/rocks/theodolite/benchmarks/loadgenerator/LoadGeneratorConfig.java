package rocks.theodolite.benchmarks.loadgenerator;

/**
 * Configuration of a load generator.
 */
public class LoadGeneratorConfig {

  private final GeneratorAction generatorAction;
  private BeforeAction beforeAction = BeforeAction.doNothing();
  private int threads = 1;

  public LoadGeneratorConfig(final GeneratorAction generatorAction) {
    this.generatorAction = generatorAction;
  }

  public LoadGeneratorConfig(final GeneratorAction generatorAction, final int threads) {
    this(generatorAction);
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
    return new LoadGeneratorExecution(workloadDefinition, this.generatorAction, this.threads);
  }

}
