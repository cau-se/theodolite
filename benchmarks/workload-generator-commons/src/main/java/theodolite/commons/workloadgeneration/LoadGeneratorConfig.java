package theodolite.commons.workloadgeneration;

/**
 * Configuration of a load generator.
 */
public class LoadGeneratorConfig {

  private final MessageGenerator messageGenerator;
  private BeforeAction beforeAction = BeforeAction.doNothing();
  private int threads = 1;

  public LoadGeneratorConfig(final MessageGenerator messageGenerator) {
    this.messageGenerator = messageGenerator;
  }

  public LoadGeneratorConfig(
      final MessageGenerator messageGenerator,
      final int threads) {
    this.messageGenerator = messageGenerator;
    this.threads = threads;
  }

  public LoadGeneratorExecution buildLoadGeneratorExecution(
      final WorkloadDefinition workloadDefinition) {
    return new LoadGeneratorExecution(workloadDefinition, this.messageGenerator, this.threads);
  }

  public BeforeAction getBeforeAction() {
    return this.beforeAction;
  }

  public void setThreads(final int threads) {
    this.threads = threads;
  }

  public void setBeforeAction(final BeforeAction beforeAction) {
    this.beforeAction = beforeAction;
  }



}
