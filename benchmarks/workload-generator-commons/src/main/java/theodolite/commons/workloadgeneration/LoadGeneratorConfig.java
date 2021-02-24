package theodolite.commons.workloadgeneration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadGeneratorConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadGeneratorConfig.class);

  private final MessageGenerator messageGenerator;
  private BeforeAction beforeAction = BeforeAction.doNothing();
  private int threads = 1;

  public LoadGeneratorConfig(final MessageGenerator messageGenerator) {
    this.messageGenerator = messageGenerator;
  }

  public LoadGeneratorConfig(
      final MessageGenerator messageGenerator,
      final BeforeAction beforeAction,
      final int threads) {
    this.messageGenerator = messageGenerator;
    this.beforeAction = beforeAction;
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
