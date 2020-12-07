package theodolite.commons.workloadgeneration.functions;

/**
 * Describes the before action which is executed before every sub experiment.
 */
@FunctionalInterface
public interface BeforeAction {

  public void run();

}
